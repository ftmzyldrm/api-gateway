package com.example.apigateway.repo;

import com.example.apigateway.model.Apis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TestRedisConfiguration.class)
public class ApiRepositoryIntegrationTest {

    @Autowired
    private ApisRepository apisRepository;
    @Test
    public void shouldSaveApis_toRedis() {
        Apis api = new Apis("myPath",true,true,"X-API-KEY");
        apisRepository.save(api);
        Apis saved= apisRepository.findById("myPath");
        assertNotNull(saved);

    }

}
