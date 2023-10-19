package com.casino.auth.exception;

public class UnauthorizedRoleException extends RuntimeException{
    public UnauthorizedRoleException(String exceptionMessage){
        super(exceptionMessage);
    }
}
