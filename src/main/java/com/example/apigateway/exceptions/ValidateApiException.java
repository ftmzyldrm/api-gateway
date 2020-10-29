package com.example.apigateway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ValidateApiException extends ResponseStatusException {

    public ValidateApiException(String errorMessage){
        this(HttpStatus.NOT_ACCEPTABLE,errorMessage);
    }

    public ValidateApiException(HttpStatus status,String message) {
        super(status,message);
    }
}
