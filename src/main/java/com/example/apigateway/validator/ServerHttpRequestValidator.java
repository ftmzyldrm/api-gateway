package com.example.apigateway.validator;

import com.example.apigateway.config.RMapBasedRedissonBackend;
import com.example.apigateway.exceptions.Error;
import com.example.apigateway.exceptions.ValidateApiException;
import com.example.apigateway.model.ApiKey;
import com.example.apigateway.model.Path;
import com.example.apigateway.model.Plan;
import com.example.apigateway.repo.PathRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

@Setter
@Component
@RequiredArgsConstructor
public class ServerHttpRequestValidator {

    private final PathRepository pathRepository;
    private final RMapBasedRedissonBackend rMapBasedRedissonBackend;
    private ServerHttpRequest serverHttpRequest;
    private ApiKey apiKey;


    public Path validateAndGetPath() {
        final String path = serverHttpRequest.getPath().toString();
        if (!path.equals(apiKey.getPath())) {
            throw new ValidateApiException(Error.API_PATH_NOT_MATCHED.getMessageParameters());
        } else {
            Path validPath = pathRepository.findById(path)
                    .orElseThrow(() -> new ValidateApiException(Error.PATH_NOT_FOUND.getMessageParameters(path)));
            if (!validPath.isActive()) {
                throw new ValidateApiException(Error.API_DISABLED.getMessageParameters(path));
            }
            return validPath;
        }
    }


    public Plan validateAndGetPathPlans(final Path path) {
        Map<String, Plan> pathPlans = path.getPlans();
        String planId = apiKey.getPlanId();
        if (!pathPlans.containsKey(planId)) {
            throw new ValidateApiException(Error.PATH_PLAN_NOT_FOUND.getMessageParameters(planId));
        }
        Plan plan = pathPlans.get(planId);
        if (serverHttpRequest.getMethod() != null) {
            String method = serverHttpRequest.getMethod().name();
            String host = serverHttpRequest.getURI().getHost();
            if (!plan.getIpWhiteList().contains(host)) {
                throw new ValidateApiException(Error.IP_NOT_ALLOWED.getMessageParameters(serverHttpRequest.getRemoteAddress()));
            }
            if (plan.getIpBlackList().contains(host)) {
                throw new ValidateApiException(Error.IP_NOT_ALLOWED.getMessageParameters(serverHttpRequest.getRemoteAddress()));
            }
            if (!plan.getSupportedMethods().contains(method)) {
                throw new ValidateApiException(Error.METHOD_NOT_ALLOWED.getMessageParameters(method));
            }
        }
        return plan;
    }


    public Bucket getBucketFromGrid(Path path, Plan plan) {
        if (path.isHasPublicPlan()) {
            //TODO: new collection should be created if not exist
            return null;
        } else {
            return rMapBasedRedissonBackend.builder()
                    .buildProxy(apiKey.getId(), getConfigSupplier(apiKey.getId(), plan));
        }
    }


    private Supplier<BucketConfiguration> getConfigSupplier(String apiKey, Plan plan) {
        return () -> {
            if (apiKey.startsWith("X")) {
                //
                if (rMapBasedRedissonBackend.getProxyConfiguration(apiKey).isPresent()) {
                    return rMapBasedRedissonBackend.getProxyConfiguration(apiKey).get();
                }
                return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(plan.getQuota().getMaxRequests(), Duration.ofMinutes(plan.getQuota().getPeriod())))
                        .addLimit(Bandwidth.simple(plan.getRateLimiting().getMaxRequests(), Duration.ofMinutes(plan.getRateLimiting().getPeriod())))
                        .build();
            }
            return BucketConfiguration.builder()
                    .addLimit(Bandwidth.classic(3, Refill.intervally(5, Duration.ofMinutes(1))))
                    .build();
        };

    }


}

