package com.example.apigateway.validator;

import com.example.apigateway.config.RMapBasedRedissonBackend;
import com.example.apigateway.exceptions.Error;
import com.example.apigateway.exceptions.ValidateApiException;
import com.example.apigateway.model.*;
import com.example.apigateway.repo.ApiKeyRepository;
import com.example.apigateway.repo.PathRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class ApisValidator {

    final PathRepository pathRepository;
    final ApiKeyRepository apiKeyRepository;
    @Autowired
    RMapBasedRedissonBackend rMapBasedRedissonBackend;

    public ApisValidator(PathRepository pathRepository, ApiKeyRepository apiKeyRepository) {
        this.pathRepository = pathRepository;
        this.apiKeyRepository = apiKeyRepository;
    }


    @SneakyThrows
    public boolean checkApis(ServerWebExchange exchange) {
        String apiKeyHeader = null;
        if (!CollectionUtils.isEmpty(exchange.getRequest().getHeaders().get("X-API-Key"))) {
            apiKeyHeader = exchange.getRequest().getHeaders().get("X-API-Key").get(0);
        }
        if (StringUtils.isEmpty(apiKeyHeader)) {
            throw new ValidateApiException(Error.API_KEY_HEADER_NOT_FOUND.getMessageParameters());
        }
        ApiKey apiKey = getApiKey(apiKeyHeader);
        Path validatedPath = getValidatedPath(exchange, apiKey);

        validatePathPlan(exchange, validatedPath, apiKey);
        return true;

    }

    private Path getValidatedPath(ServerWebExchange exchange, ApiKey apiKey) {
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getPath().toString();
        if (!path.equals(apiKey.getPath())) {
            throw new ValidateApiException(Error.API_PATH_NOT_MATCHED.getMessageParameters());
        } else {
            return pathRepository.findById(path)
                    .orElseThrow(() -> new ValidateApiException(Error.PATH_PLAN_NOT_FOUND.getMessageParameters(path)));
        }
    }

    private void validatePathPlan(ServerWebExchange exchange, Path validPathPlan, ApiKey apiKey) {

        ServerHttpRequest req = exchange.getRequest();
        String path = req.getPath().toString();

        if (!validPathPlan.isActive()) {
            throw new ValidateApiException(Error.API_DISABLED.getMessageParameters(path));
        }
        validatePathPlans(validPathPlan, apiKey, req);
    }

    private void validatePathPlans(Path validPathPlan, ApiKey apiKey, ServerHttpRequest req) {

        String planId = apiKey.getPlanId();
        String host = req.getURI().getHost();
        //TODO: null check
        String method = req.getMethod().name();
        if (validPathPlan.getPlans().containsKey(planId)) { //PlanId control
            Plan plan = validPathPlan.getPlans().get(planId);
            if (!plan.getIpWhiteList().contains(host))
                throw new ValidateApiException(Error.IP_NOT_ALLOWED.getMessageParameters(req.getRemoteAddress()));
            if (plan.getIpBlackList().contains(host))
                throw new ValidateApiException(Error.IP_NOT_ALLOWED.getMessageParameters(req.getRemoteAddress()));
            if (!plan.getSupportedMethods().contains(method))
                throw new ValidateApiException(Error.METHOD_NOT_ALLOWED.getMessageParameters(method));
            if (validPathPlan.isHasPublicPlan()) {
                //TODO: new collection should be created if not exist
            } else {

                getBucketFromGrid(apiKey.getId(), plan);
            }
        }
    }


    private ApiKey getApiKey(String apiKeyHeader) {
        return apiKeyRepository.findById(apiKeyHeader)
                .orElseThrow(() -> new ValidateApiException(Error.API_IS_NOT_FOUND.getMessageParameters()));
    }

    private Optional<String> getApiKeyHeader(ServerWebExchange exchange) {
        String apiKeyHeader = null;
        if (!CollectionUtils.isEmpty(exchange.getRequest().getHeaders().get("X-API-Key"))) {
            apiKeyHeader = exchange.getRequest().getHeaders().get("X-API-Key").get(0);
        }
        if (StringUtils.isEmpty(apiKeyHeader)) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        }
        return Optional.ofNullable(apiKeyHeader);
    }

    private Bucket getBucketFromGrid(String apiKey, Plan plan) {

        return rMapBasedRedissonBackend.builder().buildProxy(apiKey, getConfigSupplier(apiKey, plan));
    }

    private Supplier<BucketConfiguration> getConfigSupplier(String apiKey, Plan plan) {
        return () -> {
            if (apiKey.startsWith("X")) {
                // apisValidator.
                return rMapBasedRedissonBackend.getProxyConfiguration(apiKey)
                        .isPresent() ? rMapBasedRedissonBackend.getProxyConfiguration(apiKey).get() : BucketConfiguration.builder()
                        .addLimit(
                                Bandwidth.simple(plan.getQuota().getMaxRequests(), Duration.ofMinutes(plan.getQuota().getPeriod())))
                        .addLimit(Bandwidth.simple(plan.getRateLimiting().getMaxRequests(), Duration.ofMinutes(plan.getRateLimiting().getPeriod())))
                        .build();
            }
            return BucketConfiguration.builder()
                    .addLimit(
                            Bandwidth.classic(3, Refill.intervally(5, Duration.ofMinutes(1))))
                    .build();
        };

    }

}
