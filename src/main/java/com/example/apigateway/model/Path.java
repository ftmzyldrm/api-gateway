package com.example.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.Map;

@RedisHash("Path")
@Data
@AllArgsConstructor
public class Path implements Serializable {

    private String id;
    private String endPoint;
    private boolean isActive;
    private boolean hasPublicPlan;
    private Map<String,Plan> plans;
}
