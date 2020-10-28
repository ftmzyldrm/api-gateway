package com.example.apigateway.repo;

import com.example.apigateway.model.ApiKeys;
import com.example.apigateway.model.Apis;

public interface ApiKeysRepository {
    void save(ApiKeys apiKeys);
    ApiKeys findById(String id);
}
