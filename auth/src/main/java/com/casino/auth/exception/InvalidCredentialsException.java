package com.casino.auth.exception;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(String exceptionMessage){
        super(exceptionMessage);
    }
}
