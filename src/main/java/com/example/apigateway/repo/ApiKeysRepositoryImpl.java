package com.example.apigateway.repo;

import com.example.apigateway.model.ApiKeys;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

@Repository
public class ApiKeysRepositoryImpl implements ApiKeysRepository {

    private RedissonClient redisson;

    public ApiKeysRepositoryImpl(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public void save(ApiKeys apiKeys) {
        RMap<String, ApiKeys> apiKeysRMap = redisson.getMap("apiKeys");
        apiKeysRMap.putIfAbsent(apiKeys.getApiKeyHeader(),apiKeys);



    }

    @Override
    public ApiKeys findById(String id) {
        return (ApiKeys) redisson.getMap("apiKeys").get(id);
    }
}
