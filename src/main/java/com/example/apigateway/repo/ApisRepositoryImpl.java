package com.example.apigateway.repo;


import com.example.apigateway.model.Apis;
import org.springframework.data.redis.core.HashOperations;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class ApisRepositoryImpl implements ApisRepository{
    private RedisTemplate<String, Object> redisTemplate;
    private HashOperations hashOperations;

    public ApisRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public void save(Apis apis) {
        hashOperations.put("apis", apis.getPath(), apis);
    }

    @Override
    public Map<String, Apis> findAll() {
        return hashOperations.entries("apis");
    }

    @Override
    public Apis findById(String path) {
        return (Apis) hashOperations.get("apis", path);
    }

    @Override
    public void update(Apis apis) {
        save(apis);
    }

    @Override
    public void delete(String path) {
        hashOperations.delete("apis", path);
    }
}
