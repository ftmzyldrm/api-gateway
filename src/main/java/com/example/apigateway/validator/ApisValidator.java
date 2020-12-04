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
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;
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
        this.apiKeyRepository= apiKeyRepository;
    }



    @SneakyThrows
    public boolean checkApis(ServerWebExchange exchange) {
        ServerHttpRequest req = exchange.getRequest();
        String apiKeyHeader=null;
        if (exchange.getRequest().getHeaders().get("X-API-Key") != null) {
            apiKeyHeader = exchange.getRequest().getHeaders().get("X-API-Key").get(0);
        }

        if (StringUtils.isEmpty(apiKeyHeader)) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            byte[] bytes = "Missing Header: X-API-Key".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return false;
        }
        String path = req.getPath().toString();
        String host = req.getURI().getHost();
        String method = req.getMethod().name();
        Optional<ApiKey> apiKey= apiKeyRepository.findById(apiKeyHeader);

       if(!apiKey.isPresent())
           throw  new ValidateApiException(Error.API_IS_NOT_FOUND.getMessageParameters());
       else {
           if(!path.equals(apiKey.get().getPath())){
               throw  new ValidateApiException(Error.API_PATH_NOT_MATCHED.getMessageParameters());
           }
           else {
              Optional<Path> pathPlan= pathRepository.findById(path);
              if(!pathPlan.isPresent())
                  throw new ValidateApiException(Error.PATH_PLAN_NOT_FOUND.getMessageParameters(path));
              else {
                Path validPathPlan = pathPlan.get();
                String planId= apiKey.get().getPlanId();
                if( !validPathPlan.isActive())
                    throw new ValidateApiException(Error.API_DISABLED.getMessageParameters(path));
                  if(validPathPlan.getPlans().containsKey(planId)) { //PlanId control
                      Plan plan = validPathPlan.getPlans().get(planId);
                      if (!plan.getIpWhiteList().contains(host))
                          throw new ValidateApiException(Error.IP_NOT_ALLOWED.getMessageParameters(req.getRemoteAddress()));
                      if (plan.getIpBlackList().contains(host))
                          throw new ValidateApiException(Error.IP_NOT_ALLOWED.getMessageParameters(req.getRemoteAddress()));
                      if(!plan.getSupportedMethods().contains(method))
                          throw new ValidateApiException(Error.METHOD_NOT_ALLOWED.getMessageParameters(method));
                      if (validPathPlan.isHasPublicPlan()) {
                          //whiteList control




                          //Plan..getRemoteAddress()
                      }
                      if(!validPathPlan.isHasPublicPlan()){

                          plan.getQuota();

                      }


                  }



              }
           }
       }

        return true;

    }

    private  Bucket getBucketFromGrid(String apiKey,Quota quota, RateLimiting rateLimiting){


        Bucket   bucket = rMapBasedRedissonBackend.builder().buildProxy(apiKey, getConfigSupplier(apiKey,quota,rateLimiting));



        return bucket;

    }
    private Supplier<BucketConfiguration> getConfigSupplier(String apiKey,Quota quota, RateLimiting rateLimiting) {
        return () -> {
            if (apiKey.startsWith("X")) {
                // apisValidator.
                return rMapBasedRedissonBackend.getProxyConfiguration(apiKey)
                        .isPresent() ? rMapBasedRedissonBackend.getProxyConfiguration(apiKey).get() : BucketConfiguration.builder()
                        .addLimit(
                                Bandwidth.simple(quota.getMaxRequests(),Duration.ofMinutes(quota.getPeriod())))
                        .addLimit(Bandwidth.simple(rateLimiting.getMaxRequests(),Duration.ofMinutes(rateLimiting.getPeriod())))
                        .build();
            }
            return BucketConfiguration.builder()
                    .addLimit(
                            Bandwidth.classic(3, Refill.intervally(5, Duration.ofMinutes(1))))
                    .build();
        };
    }

}
