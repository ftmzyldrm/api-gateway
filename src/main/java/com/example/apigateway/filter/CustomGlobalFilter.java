package com.example.apigateway.filter;

import com.example.apigateway.exceptions.Error;
import com.example.apigateway.exceptions.ValidateApiException;
import com.example.apigateway.model.ApiKey;
import com.example.apigateway.model.Path;
import com.example.apigateway.model.Plan;
import com.example.apigateway.repo.ApiKeyRepository;
import com.example.apigateway.validator.ServerHttpRequestValidator;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private final ApiKeyRepository apiKeyRepository;
    private final ServerHttpRequestValidator validator;

    AtomicLong consumed = new AtomicLong(1);
    private final Bucket freeBucket = Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
            .build();


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        configureValidator(exchange);
        Path path = validator.validateAndGetPath();
        Plan plan = validator.validateAndGetPathPlans(path);
        Bucket bucket = validator.getBucketFromGrid(path, plan);
        log.info(" new probe--> {}", bucket.getAvailableTokens());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> log.info("X-Rate-Limit-Remaining {}", probe.getRemainingTokens())));
        }
        exchange.getResponse().getHeaders().add("X-Rate-Limit-Retry-After-Milliseconds",
                Long.toString(TimeUnit.NANOSECONDS.toMillis(probe.getNanosToWaitForRefill())));

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            log.info(" retry after milliseconds:{} ", TimeUnit.NANOSECONDS.toMillis(probe.getNanosToWaitForRefill()));
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        }));

    }

    @Override
    public int getOrder() {
        return -1;
    }

    private void configureValidator(ServerWebExchange exchange) {
        validator.setApiKey(getApiKey(exchange.getRequest()));
        validator.setServerHttpRequest(exchange.getRequest());
    }

    private ApiKey getApiKey(ServerHttpRequest serverHttpRequest) {
        return apiKeyRepository.findById(getApiKeyHeader(serverHttpRequest))
                .orElseThrow(() -> new ValidateApiException(Error.API_IS_NOT_FOUND.getMessageParameters()));
    }

    private String getApiKeyHeader(ServerHttpRequest serverHttpRequest) {
        String apiKeyHeader = null;
        if (!CollectionUtils.isEmpty(serverHttpRequest.getHeaders().get("X-API-Key"))) {
            apiKeyHeader = serverHttpRequest.getHeaders().get("X-API-Key").get(0);
        }
        if (StringUtils.isEmpty(apiKeyHeader)) {
            throw new ValidateApiException(Error.API_KEY_HEADER_NOT_FOUND.getMessageParameters());
        }
        return apiKeyHeader;
    }
}
