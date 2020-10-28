package com.example.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Apis implements Serializable {
    private String path;
    private boolean enabled;
    private boolean isPublic;
    private String apiKeyHeader;


}
