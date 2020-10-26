package com.example.apigateway.repo;

import com.example.apigateway.model.ApiKeys;
import io.github.bucket4j.Bandwidth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class ApiKeysRepositoryTest {
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Test
    public void saveAndGet() {
        List<String> allowedMethods = Arrays.asList("POST", "GET");
        List<Bandwidth> rateLimitingBandWidths = Arrays.asList(Bandwidth.simple(100L, Duration.ofSeconds(3600)));
        List<String> allowedIps = Arrays.asList("10.254.182.40", "127.0.0.1");
        ApiKeys apiKeys = new ApiKeys(allowedMethods, rateLimitingBandWidths, allowedIps);
        // redisTemplate.opsForValue().set("apiKeys",apiKeys);
        redisTemplate.opsForHash().put("apiKeys", "api_key", apiKeys);

    }
}
