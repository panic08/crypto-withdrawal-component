package com.casino.replenishments.exception;

public class UnauthorizedRoleException extends RuntimeException{
    public UnauthorizedRoleException(String exceptionMessage){
        super(exceptionMessage);
    }
}
