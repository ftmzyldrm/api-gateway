package com.example.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
@AllArgsConstructor
public class Plan implements Serializable {
    private String id;
    private AuthenticationType authenticationType;
    private List<String> ipWhiteList;
    private List<String> ipBlackList;
    private List<String> supportedMethods;
    private Quota quota;
    private RateLimiting rateLimiting;

}
