package com.example.apigateway.model;


import org.springframework.boot.actuate.trace.http.HttpTrace;

import java.io.Serializable;

public class ContentTrace implements Serializable {

    protected  HttpTrace httpTrace;

    public HttpTrace getHttpTrace() {
        return httpTrace;
    }

    protected String requestBody;

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    protected String responseBody;


    public ContentTrace() {
    }

    public void setHttpTrace(HttpTrace httpTrace) {
        this.httpTrace = httpTrace;
    }
}