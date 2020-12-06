package com.example.apigateway.validator;

import com.example.apigateway.config.RMapBasedRedissonBackend;
import com.example.apigateway.model.*;
import com.example.apigateway.repo.ApiKeyRepository;
import com.example.apigateway.repo.PathRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ServerHttpRequestValidatorTest {

    @Mock
    ApiKeyRepository apiKeysRepository;

    @Mock
    PathRepository pathRepository;
    @Mock
    RMapBasedRedissonBackend rMapBasedRedissonBackend;
    @InjectMocks
    ServerHttpRequestValidator serversHttpRequestValidator;

    MockServerWebExchange exchange;

    @BeforeEach
    void init() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost/test").build();
        exchange = MockServerWebExchange.from(request);
        serversHttpRequestValidator = new ServerHttpRequestValidator(pathRepository, rMapBasedRedissonBackend);
        serversHttpRequestValidator.setApiKey(getApiKey());
        serversHttpRequestValidator.setServerHttpRequest(exchange.getRequest());
    }

    @Test
    void shouldAllowIfGivenPathIsPublic() {
        //given:
        ApiKey api = getApiKey();
        Path path = getPath(true, true);
        //when:
       // Mockito.when(apiKeysRepository.findById("532452")).thenReturn(java.util.Optional.of(api));
        Mockito.when(pathRepository.findById("/test")).thenReturn(java.util.Optional.of(path));
        Path actualPath = serversHttpRequestValidator.validateAndGetPath();
        //then:
        Assertions.assertEquals(path, actualPath);
    }

    private static Path getPath(boolean isActive, boolean hasPublicPlan) {
        return new Path("/test", "www.google.com", isActive, hasPublicPlan, getPlansMap());
    }

    private static Map<String, Plan> getPlansMap() {
        Map<String, Plan> plans = new HashMap<>();
        plans.put("12334", getPlan());
        return plans;
    }

    private static Plan getPlan() {
        return new Plan("12334", AuthenticationType.API_KEY, Collections.singletonList("localhost"), Collections.singletonList
                ("10.254.150.32"), Collections.singletonList("GET"), getQuota(), getRateLimiting());
    }

    private static RateLimiting getRateLimiting() {
        return new RateLimiting(10, 1, "MONTHS");
    }

    private static Quota getQuota() {
        return new Quota(10, 1, "MINUTES");
    }

    private static ApiKey getApiKey() {
        return new ApiKey("532452", "/test", "12334");
    }

//    @Test
//    public  void whenApiDisableExceptionThrown_thenAssertionSucceeds(){
//        ApiKey api = getApiKey();
//        Mockito.when(pathRepository.findById("/test")).thenReturn(api);
//
//
//       Exception exception= assertThrows(ValidateApiException.class,()->{
//           serversHttpRequestValidator.checkApis(exchange);
//        });
//
//        String expectedMessage = Error.API_DISABLED.getMessageParameters();
//        String actualMessage = exception.getMessage();
//
//        assertTrue(actualMessage.contains(expectedMessage));
//    }
//
//    @Test
//    public void whenApiIsNotPublic_AndAllowedIpsDoesntContainExceptionThrown_thenAssertionSucceeds(){
//        ApiKey api = new Apis("/test", true, false, "X-API-KEY");
//        Mockito.when(pathRepository.findById("/test")).thenReturn(api);
//        List<String> allowedIps = Arrays.asList("10.254.180.41");
//        List<String> allowedMethods = Arrays.asList("POST","GET");
//        Bandwidth bandwith = Bandwidth.classic(100, Refill.greedy(10, Duration.ofMinutes(2)));
//
//        ApiKeys apiKeys = new ApiKeys("X-API-KEY",allowedMethods,Arrays.asList(bandwith),allowedIps);
//        Mockito.when(apiKeysRepository.findById("X-API-KEY")).thenReturn(apiKeys);
//
//
//        Exception exception= assertThrows(ValidateApiException.class,()->{
//            apisValidator.checkApis(exchange);
//        });
//
//        String expectedMessage = Error.IP_NOT_ALLOWED.getMessageParameters("localhost");
//        String actualMessage = exception.getMessage();
//        assertTrue(actualMessage.contains(expectedMessage));
//
//    }
//    @Test
//    public void whenApiIsNotPublic_AndAllowedMethodDoesntMatchExceptionThrown_thenAssertionSucceeds(){
//        Apis api = new Apis("/test", true, false, "X-API-KEY");
//        Mockito.when(pathRepository.findById("/test")).thenReturn(api);
//        List<String> allowedIps = Arrays.asList("localhost","10.254.180.41");
//        List<String> allowedMethods = Arrays.asList("POST");
//        Bandwidth bandwith = Bandwidth.classic(100, Refill.greedy(10, Duration.ofMinutes(2)));
//
//        ApiKeys apiKeys = new ApiKeys("X-API-KEY",allowedMethods,Arrays.asList(bandwith),allowedIps);
//        Mockito.when(apiKeysRepository.findById("X-API-KEY")).thenReturn(apiKeys);
//
//
//        Exception exception= assertThrows(ValidateApiException.class,()->{
//            apisValidator.checkApis(exchange);
//        });
//
//        String expectedMessage = Error.METHOD_NOT_ALLOWED.getMessageParameters("GET");
//        String actualMessage = exception.getMessage();
//        assertTrue(actualMessage.contains(expectedMessage));
//    }
//


}
