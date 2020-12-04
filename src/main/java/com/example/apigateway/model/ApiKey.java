package com.example.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@RedisHash("ApiKey")
@AllArgsConstructor
public class ApiKey implements Serializable {
   // @Id
    private String id;

    private String path;

    private String planId;
}
