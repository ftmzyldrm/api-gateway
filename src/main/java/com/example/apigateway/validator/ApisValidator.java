package com.example.apigateway.validator;

import com.example.apigateway.model.Apis;
import com.example.apigateway.repo.ApisRepository;
import org.springframework.stereotype.Component;

@Component
public class ApisValidator {

    final ApisRepository apisRepository;

    public ApisValidator(ApisRepository apisRepository) {
        this.apisRepository = apisRepository;
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


}
