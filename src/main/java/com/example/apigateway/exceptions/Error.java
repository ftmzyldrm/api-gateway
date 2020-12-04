package com.example.apigateway.exceptions;

import lombok.Getter;

import java.text.MessageFormat;


@Getter
public enum Error {

    API_DISABLED(1,"Api is disabled"){
        @Override
        public String getMessageParameters(Object... args){
            return API_DISABLED.message;
        }
    },
    IP_NOT_ALLOWED(2, "IP is not allowed {0}"){
        @Override
        public String getMessageParameters(Object... args){
            return MessageFormat.format(IP_NOT_ALLOWED.message,args);
        }
    },
    METHOD_NOT_ALLOWED(3,"Method is not allowed {0}" ){
        @Override
        public String getMessageParameters(Object... args){
            return MessageFormat.format(METHOD_NOT_ALLOWED.message,args);
        }
    },
    API_IS_NOT_FOUND(4,"API is not found"){
        @Override
        public String getMessageParameters(Object... args){
            return MessageFormat.format(API_IS_NOT_FOUND.message,args);
        }
    },
    API_PATH_NOT_MATCHED(5,"API path is not matched"){
        @Override
        public String getMessageParameters(Object... args){
            return MessageFormat.format(API_PATH_NOT_MATCHED.message,args);
        }
    },
    PATH_PLAN_NOT_FOUND(6,"Path Plan is not found {0}" ){
        @Override
        public String getMessageParameters(Object... args){
            return MessageFormat.format(API_PATH_NOT_MATCHED.message,args);
        }
    };


    private final int errorCode;
    private final String message;

    Error(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public abstract String getMessageParameters(Object... args);


}
