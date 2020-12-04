package com.example.apigateway.repo;

import com.example.apigateway.model.ApiKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ApiKeyIntegrationTest {
    @Autowired
    ApiKeyRepository apiKeyRepository;

    @Test
    public void shouldSaveApiKey_toRedis() {

        ApiKey apiKey = new ApiKey("c151148d234234","/testplan1","432sferwe");
        apiKeyRepository.save(apiKey);
        ApiKey saved =apiKeyRepository.findById("c151148d234234").get();

        assertNotNull(saved);

    }
}
