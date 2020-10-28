package com.example.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest req = exchange.getRequest();
        RequestPath path = req.getPath();
        log.info("RequestPAth {}", path.toString());
        URI url = req.getURI();
        String host = url.getHost();
        log.info("host {}", host);
        HttpMethod httpMethod = req.getMethod();
        log.info("httpMethod {}", httpMethod.name());

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
