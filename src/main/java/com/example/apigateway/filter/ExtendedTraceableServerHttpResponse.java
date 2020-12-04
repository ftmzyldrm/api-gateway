package com.example.apigateway.filter;

import org.springframework.boot.actuate.trace.http.TraceableResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class ExtendedTraceableServerHttpResponse implements TraceableResponse {

    private final int status;

    private final Map<String, List<String>> headers;

    ExtendedTraceableServerHttpResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        this.status = (response.getStatusCode() != null) ? response.getStatusCode().value() : HttpStatus.OK.value();
        this.headers = new LinkedHashMap<>(response.getHeaders());
        //Remove the cached body to ease garbage collection in case of large response bodies
        Object cachedResponseBodyObject = exchange.getAttributes().remove("cachedResponseBodyObject");
        if (cachedResponseBodyObject != null) {
            this.headers.put("response_body", singletonList(cachedResponseBodyObject.toString()));
        }
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

}