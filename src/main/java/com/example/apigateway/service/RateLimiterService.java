package com.example.apigateway.service;

import com.example.apigateway.config.RMapBasedRedissonBackend;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RateLimiterService {

    private static RateLimiterService instance;

    private RateLimiterService(){}

    public static synchronized RateLimiterService getInstance(){
        if(instance == null){
            instance = new RateLimiterService();
        }
        return instance;
    }

    @Autowired
    RMapBasedRedissonBackend rMapBasedRedissonBackend;

    public int getBucketsSize(){
        return rMapBasedRedissonBackend.getBuckets().size();
    }

    public  Bucket getBucketFromGrid(String apiKey){

     Optional<BucketConfiguration> bucketConfiguration = rMapBasedRedissonBackend.getProxyConfiguration(apiKey);
     Bucket bucket = null;
     if(bucketConfiguration.isPresent()){
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(10, Duration.ofSeconds(1)))
                .build();
      // bucket= rMapBasedRedissonBackend.getBuckets().get("X-API-KEY");

      bucket = rMapBasedRedissonBackend.builder().buildProxy("X-API-KEY", configuration);

     }

        return bucket;

    }
}
