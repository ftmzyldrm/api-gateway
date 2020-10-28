package com.example.apigateway.model;

import io.github.bucket4j.Bandwidth;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class ApiKeys implements Serializable {
    private String apiKeyHeader;
    private List<String> allowedMethods;
    private List<Bandwidth> rateLimitingBandWidths;
    private List<String> allowedIps;
}
