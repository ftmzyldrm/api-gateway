package com.example.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.Include;
import org.springframework.boot.actuate.web.trace.reactive.HttpTraceWebFilter;
import org.springframework.core.Ordered;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Component
@Slf4j
public class ExtendedHttpTraceWebFilter extends HttpTraceWebFilter implements WebFilter, Ordered {

    private static final Object NONE = new Object();

    // Not LOWEST_PRECEDENCE, but near the end, so it has a good chance of catching all
    // enriched headers, but users can add stuff after this if they want to
    private int order = Ordered.LOWEST_PRECEDENCE - 10;

    private final HttpTraceRepository repository;

    private final HttpExchangeTracer tracer;

    private final Set<Include> includes;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;


    public ExtendedHttpTraceWebFilter(HttpTraceRepository repository, HttpExchangeTracer tracer, Set<Include> includes) {
        super(repository, tracer, includes);
        this.repository = repository;
        this.tracer = tracer;
        this.includes = includes;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Mono<?> principal = (this.includes.contains(Include.PRINCIPAL)
                ? exchange.getPrincipal().cast(Object.class).defaultIfEmpty(NONE) : Mono.just(NONE));
        Mono<?> session = (this.includes.contains(Include.SESSION_ID) ? exchange.getSession() : Mono.just(NONE));
        return Mono.zip(principal, session).flatMap((tuple) -> filter(exchange, chain,
                asType(tuple.getT1(), Principal.class), asType(tuple.getT2(), WebSession.class)));
    }

    private <T> T asType(Object object, Class<T> type) {
        if (type.isInstance(object)) {
            return type.cast(object);
        }
        return null;
    }

    private Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain, Principal principal,
                              WebSession session) {
        exchange.getResponse().beforeCommit(() -> {
            //Request needs to be read after other filters including spring cloud gateway
            //so that the request body gets cached as exchange attribute
            //We therefore run both request and response after response is received
            ExtendedServerWebExchangeTraceableRequest request = new ExtendedServerWebExchangeTraceableRequest(exchange);
            HttpTrace trace = this.tracer.receivedRequest(request);
            ExtendedTraceableServerHttpResponse response = new ExtendedTraceableServerHttpResponse(exchange);
            this.tracer.sendingResponse(trace, response, () -> principal, () -> getStartedSessionId(session));
            kafkaTemplate.send(topicName,response.toString());
            log.info("My request is->>{}",request.toString());
            log.info("My response is -->{}", response.toString());
            kafkaTemplate.send("response_topic",response.toString());

            this.repository.add(trace);
            return Mono.empty();
        });
        return chain.filter(exchange);
    }

    private String getStartedSessionId(WebSession session) {
        return (session != null && session.isStarted()) ? session.getId() : null;
    }

}