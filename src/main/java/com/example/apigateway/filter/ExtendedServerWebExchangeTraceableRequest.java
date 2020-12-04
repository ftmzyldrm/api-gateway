package com.example.apigateway.filter;

import org.springframework.boot.actuate.trace.http.TraceableRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public class ExtendedServerWebExchangeTraceableRequest implements TraceableRequest {

    private final String method;

    private final Map<String, List<String>> headers;

    private final URI uri;

    private final String remoteAddress;

    ExtendedServerWebExchangeTraceableRequest(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        this.method = request.getMethodValue();
        this.headers = new HashMap(request.getHeaders());
        //Remove the cached body to ease garbage collection in case of large response bodies
        Object cachedRequestBodyObject = exchange.getAttributes().remove("cachedRequestBodyObject");
        if (cachedRequestBodyObject != null) {
            this.headers.put("request_body", singletonList(cachedRequestBodyObject.toString()));
        }
        this.uri = request.getURI();
        this.remoteAddress = getRemoteAddress(request);
    }

    private static String getRemoteAddress(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        InetAddress address = (remoteAddress != null) ? remoteAddress.getAddress() : null;
        return (address != null) ? address.toString() : null;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return new LinkedHashMap<>(this.headers);
    }

    @Override
    public String getRemoteAddress() {
        return this.remoteAddress;
    }

}
