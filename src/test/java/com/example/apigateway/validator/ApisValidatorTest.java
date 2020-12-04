package com.example.apigateway.validator;

import com.example.apigateway.repo.ApiKeyRepository;
import com.example.apigateway.repo.PathRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

@ExtendWith(MockitoExtension.class)
public class ApisValidatorTest {

    @Mock
    ApiKeyRepository apiKeysRepository;

    @Mock
    PathRepository pathRepository;

    @InjectMocks
    ApisValidator apisValidator;
    MockServerWebExchange exchange;
    @BeforeEach
    void init(){
        apisValidator = new ApisValidator(pathRepository,apiKeysRepository);
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost/test").build();
        exchange = MockServerWebExchange.from(request);
    }

//    @Test
//    public void shouldAllowIfGivenPathIsPublic(){
//        ApiKey api = new ApiKey("532452", "/test", "12334" );
//        Mockito.when(apiKeysRepository.findById("/532452")).thenReturn(java.util.Optional.of(api));
//        boolean isPublic  = apisValidator.checkApis(exchange);
//        Assertions.assertEquals(isPublic,true);
//    }
//
//    @Test
//    public  void whenApiDisableExceptionThrown_thenAssertionSucceeds(){
//        ApiKey api =new ApiKey("532452", "/test", "12334" );
//        Mockito.when(pathRepository.findById("/test")).thenReturn(api);
//
//
//       Exception exception= assertThrows(ValidateApiException.class,()->{
//            apisValidator.checkApis(exchange);
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




}
