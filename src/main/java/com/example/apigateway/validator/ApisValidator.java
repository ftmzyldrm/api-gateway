package com.example.apigateway.validator;

import com.example.apigateway.exceptions.Error;
import com.example.apigateway.exceptions.ValidateApiException;
import com.example.apigateway.model.ApiKeys;
import com.example.apigateway.model.Apis;
import com.example.apigateway.repo.ApiKeysRepository;
import com.example.apigateway.repo.ApisRepository;
import lombok.SneakyThrows;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class ApisValidator {

    final ApisRepository apisRepository;
    final ApiKeysRepository apiKeysRepository;

    public ApisValidator(ApisRepository apisRepository, ApiKeysRepository apiKeysRepository) {
        this.apisRepository = apisRepository;
        this.apiKeysRepository= apiKeysRepository;
    }


    public void insertApis() {
        System.out.println("Apis print ");
        Apis user = new Apis("test", true, true, "X-API-KEY");

        apisRepository.save(user);
        Apis apis = new Apis("test2", false, true, "X-API-KEY");
        apisRepository.save(apis);
//       Apis getApis =apisRepository.findById("test");
//       Map<String, Apis> apisMap=apisRepository.findAll();
//        for (Map.Entry e:apisMap.entrySet()
//             ) {
//            System.out.println(e.getKey());
//        }
//        System.out.println("getApis-->" +getApis.getPath());
    }

    @SneakyThrows
    public boolean checkApis(ServerWebExchange exchange) {
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getPath().toString();
        String host = req.getURI().getHost();
        String method = req.getMethod().name();
       Apis api= apisRepository.findById(path);
       if(!api.isEnabled())
           throw  new ValidateApiException(Error.API_DISABLED.getMessageParameters());
       if(!api.isPublic()){
           api.getApiKeyHeader();
          ApiKeys apiKeys= apiKeysRepository.findById(api.getApiKeyHeader());
         if( !apiKeys.getAllowedIps().contains(host))
             throw new ValidateApiException(Error.IP_NOT_ALLOWED.getMessageParameters(host));
         if(!apiKeys.getAllowedMethods().contains(method))
             throw new ValidateApiException(Error.METHOD_NOT_ALLOWED.getMessageParameters(method));
       }
       return api.isPublic();


    }

    public void checkRateLimiting(String path){

        ApiKeys apiKeys= apiKeysRepository.findById(path);
        apiKeys.getRateLimitingBandWidths();

    }
}
