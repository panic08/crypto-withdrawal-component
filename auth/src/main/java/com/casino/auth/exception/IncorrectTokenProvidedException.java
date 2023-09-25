package com.casino.auth.exception;

public class IncorrectTokenProvidedException extends RuntimeException{
    public IncorrectTokenProvidedException(String exceptionMessage){
        super(exceptionMessage);
    }
}
