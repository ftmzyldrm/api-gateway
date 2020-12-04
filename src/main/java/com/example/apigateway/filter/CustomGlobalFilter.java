package com.example.apigateway.filter;

import com.example.apigateway.config.RMapBasedRedissonBackend;
import com.example.apigateway.validator.ApisValidator;
import io.github.bucket4j.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    ApisValidator apisValidator;
    @Autowired
    RMapBasedRedissonBackend rMapBasedRedissonBackend;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    AtomicLong consumed = new AtomicLong(1);
    private final Bucket freeBucket = Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
            .build();




    @Value("${kafka.topic.name}")
    private String topicName;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest req = exchange.getRequest();
        String apiKeyHeader= req.getHeaders().get("X-API-KEY").get(0);

        Bucket bucket= getBucketFromGrid("API-KEY");
        log.info(" new probe--> {}", bucket.getAvailableTokens());
        ConsumptionProbe probe= bucket.tryConsumeAndReturnRemaining(1);
        if(probe.isConsumed()){
            exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining",
                    Long.toString(probe.getRemainingTokens()));
            return chain.filter(exchange)
                    .then(Mono.fromRunnable(()->
            {
                log.info("X-Rate-Limit-Remaining {}",probe.getRemainingTokens());

            }
            ));
        }

        exchange.getResponse().getHeaders().add("X-Rate-Limit-Retry-After-Milliseconds",
                Long.toString(TimeUnit.NANOSECONDS.toMillis(probe.getNanosToWaitForRefill())));

        return chain.filter(exchange)
                .then(Mono.fromRunnable(()
                                -> {
                   log.info(" retry after milliseconds:{} ",TimeUnit.NANOSECONDS.toMillis(probe.getNanosToWaitForRefill()) );
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

                        }
                ));

    }
    private  Bucket getBucketFromGrid(String apiKey){


         Bucket   bucket = rMapBasedRedissonBackend.builder().buildProxy(apiKey, getConfigSupplier(apiKey));



        return bucket;

    }
    private  Supplier<BucketConfiguration> getConfigSupplier(String apiKey) {
        return () -> {
            if (apiKey.startsWith("X")) {
               // apisValidator.
                return rMapBasedRedissonBackend.getProxyConfiguration(apiKey)
                        .isPresent() ? rMapBasedRedissonBackend.getProxyConfiguration(apiKey).get() : BucketConfiguration.builder()
                        .addLimit(
                                Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                        .build();
            }
            return BucketConfiguration.builder()
                    .addLimit(
                            Bandwidth.classic(3, Refill.intervally(5, Duration.ofMinutes(1))))
                    .build();
        };
    }
    @Override
    public int getOrder() {
        return -1;
    }
}
