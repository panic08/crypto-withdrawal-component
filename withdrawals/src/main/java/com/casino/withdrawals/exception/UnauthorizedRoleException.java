package com.casino.withdrawals.exception;

public class UnauthorizedRoleException extends RuntimeException{
    public UnauthorizedRoleException(String exceptionMessage){
        super(exceptionMessage);
    }
}
