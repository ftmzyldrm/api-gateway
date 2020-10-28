package com.example.apigateway.repo;


import com.example.apigateway.model.Apis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class ApisRepositoryTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void get() {
        Apis api = new Apis("myPathTest", true, true, "X-API-KEY");
        // Apis api2 = new Apis("test2",false,true,"X-API-KEY");
        redisTemplate.opsForHash().put("apis", api.getPath(), api);
        //final Apis user = (Apis) redisTemplate.opsForValue().get("apis");
    }


}
