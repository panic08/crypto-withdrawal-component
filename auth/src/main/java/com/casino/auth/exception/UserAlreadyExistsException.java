package com.casino.auth.exception;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String exceptionMessage){
        super(exceptionMessage);
    }
}
