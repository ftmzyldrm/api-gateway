package com.example.apigateway.repo;





import com.example.apigateway.config.RedisProperties;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@TestConfiguration
public class TestRedisConfiguration {

    private final RedisServer redisServer;

    public TestRedisConfiguration(final  RedisProperties myRedisProperties) {
        this.redisServer = new RedisServer(myRedisProperties.getRedisPort());
    }
                //new RedisServer(6379);


    @PostConstruct
    public void postConstruct() {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }
}
