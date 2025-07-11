package com.nageoffer.shortlink.project.common.convention.exception;


import com.nageoffer.shortlink.project.common.convention.errorcode.BaseErrorCode;
import com.nageoffer.shortlink.project.common.convention.errorcode.IErrorCode;

public class ClientException extends AbstractException {

    @Override
    public String toString() {
        return "ClientException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    public ClientException(IErrorCode errorCode){
        super(null,null,errorCode);
    }

    public ClientException(String message){
        super(message,null, BaseErrorCode.CLIENT_ERROR);
    }

    public ClientException(String message, IErrorCode errorCode){
        super(message,null,errorCode);
    }

    public ClientException(String message, Throwable throwable, IErrorCode errorCode){
        super(message,throwable,errorCode);
    }

}
