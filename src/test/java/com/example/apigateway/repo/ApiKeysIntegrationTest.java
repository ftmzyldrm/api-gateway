package com.example.apigateway.repo;


import com.example.apigateway.model.ApiKeys;
import com.example.apigateway.model.Apis;
import io.github.bucket4j.Bandwidth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ApiKeysIntegrationTest {


    @Autowired
    private ApiKeysRepository apisRepository;

    @Test
    public void shouldSaveApis_toRedis() {
        List<String> allowedMethods = Arrays.asList("POST", "GET");
        List<Bandwidth> rateLimitingBandWidths = Arrays.asList(Bandwidth.simple(100L, Duration.ofSeconds(3600)));
        List<String> allowedIps = Arrays.asList("10.254.182.40", "127.0.0.1");
        ApiKeys apiKeys = new ApiKeys("X-API-KEy",allowedMethods, rateLimitingBandWidths, allowedIps);
        apisRepository.save(apiKeys);
        ApiKeys saved =apisRepository.findById("X-API-KEy");

        assertNotNull(saved);

    }
}
