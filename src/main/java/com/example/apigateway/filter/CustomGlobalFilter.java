package com.example.apigateway.filter;

import com.example.apigateway.validator.ApisValidator;
import io.github.bucket4j.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    ApisValidator apisValidator;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    AtomicLong consumed = new AtomicLong(1);
    private static Bucket bucket;


    @Value("${kafka.topic.name}")
    private String topicName;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        ServerHttpRequest req = exchange.getRequest();

       // apisValidator.checkApis(exchange);
        //Refill refill = Refill.intervally(1, Duration.ofMinutes(10));

       // ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if(bucket==null)
          bucket= apisValidator.checkRateLimiting();
        log.info("probe--> {}" ,bucket.getAvailableTokens());
        if (bucket.tryConsume(1)){
            consumed.incrementAndGet();
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            log.info("atomic {}",consumed);

        return chain.filter(exchange);}
       // long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
//         ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
//                .header("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill))
//                .build();

       exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        String requestUri =exchange.getRequest().getURI().toString();

       // return chain.filter(exchange);
        kafkaTemplate.send(topicName, "some string value");
        return chain.filter(exchange)
                .then(Mono.fromRunnable(()
                        ->{ exchange.getResponse().setRawStatusCode(201);

                }
                ));

    }


    @Override
    public int getOrder() {
        return -1;
    }
}
