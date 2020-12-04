package com.example.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
@Data
@AllArgsConstructor
public class Request implements Serializable {
    private String uri;
    private String method;
    private String status;


}
