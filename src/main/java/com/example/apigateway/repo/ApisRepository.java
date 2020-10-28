package com.example.apigateway.repo;

import com.example.apigateway.model.Apis;

import java.util.Map;

public interface ApisRepository {
    void save(Apis apis);

    Map<String, Apis> findAll();

    Apis findById(String id);

    void update(Apis apis);

    void delete(String id);
}

