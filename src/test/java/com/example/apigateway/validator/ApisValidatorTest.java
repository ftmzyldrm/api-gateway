package com.example.apigateway.validator;

import com.example.apigateway.exceptions.Error;
import com.example.apigateway.exceptions.ValidateApiException;
import com.example.apigateway.model.ApiKeys;
import com.example.apigateway.model.Apis;
import com.example.apigateway.repo.ApiKeysRepository;
import com.example.apigateway.repo.ApisRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ApisValidatorTest {

    @Mock
    ApiKeysRepository apiKeysRepository;

    @Mock
    ApisRepository apisRepository;

    @InjectMocks
    ApisValidator apisValidator;
    MockServerWebExchange exchange;
    @BeforeEach
    void init(){
        apisValidator = new ApisValidator(apisRepository,apiKeysRepository);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost/test").build();
       exchange = MockServerWebExchange.from(request);
    }

    @Test
    public void shouldAllowIfGivenPathIsPublic(){
        Apis api = new Apis("/test", true, true, "X-API-KEY");
        Mockito.when(apisRepository.findById("/test")).thenReturn(api);
        boolean isPublic  = apisValidator.checkApis(exchange);
        Assertions.assertEquals(isPublic,true);
    }

    @Test
    public  void whenApiDisableExceptionThrown_thenAssertionSucceeds(){
        Apis api = new Apis("/test", false, true, "X-API-KEY");
        Mockito.when(apisRepository.findById("/test")).thenReturn(api);


       Exception exception= assertThrows(ValidateApiException.class,()->{
            apisValidator.checkApis(exchange);
        });

        String expectedMessage = Error.API_DISABLED.getMessageParameters();
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void whenApiIsNotPublic_AndAllowedIpsDoesntContainExceptionThrown_thenAssertionSucceeds(){
        Apis api = new Apis("/test", true, false, "X-API-KEY");
        Mockito.when(apisRepository.findById("/test")).thenReturn(api);
        List<String> allowedIps = Arrays.asList("10.254.180.41");
        List<String> allowedMethods = Arrays.asList("POST","GET");
        Bandwidth bandwith = Bandwidth.classic(100, Refill.greedy(10, Duration.ofMinutes(2)));

        ApiKeys apiKeys = new ApiKeys("X-API-KEY",allowedMethods,Arrays.asList(bandwith),allowedIps);
        Mockito.when(apiKeysRepository.findById("X-API-KEY")).thenReturn(apiKeys);


        Exception exception= assertThrows(ValidateApiException.class,()->{
            apisValidator.checkApis(exchange);
        });

        String expectedMessage = Error.IP_NOT_ALLOWED.getMessageParameters("localhost");
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

    }
    @Test
    public void whenApiIsNotPublic_AndAllowedMethodDoesntMatchExceptionThrown_thenAssertionSucceeds(){
        Apis api = new Apis("/test", true, false, "X-API-KEY");
        Mockito.when(apisRepository.findById("/test")).thenReturn(api);
        List<String> allowedIps = Arrays.asList("localhost","10.254.180.41");
        List<String> allowedMethods = Arrays.asList("POST");
        Bandwidth bandwith = Bandwidth.classic(100, Refill.greedy(10, Duration.ofMinutes(2)));

        ApiKeys apiKeys = new ApiKeys("X-API-KEY",allowedMethods,Arrays.asList(bandwith),allowedIps);
        Mockito.when(apiKeysRepository.findById("X-API-KEY")).thenReturn(apiKeys);


        Exception exception= assertThrows(ValidateApiException.class,()->{
            apisValidator.checkApis(exchange);
        });

        String expectedMessage = Error.METHOD_NOT_ALLOWED.getMessageParameters("GET");
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }




}
