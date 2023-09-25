package com.casino.replenishments.exception;

public class IncorrectTokenProvidedException extends RuntimeException{
    public IncorrectTokenProvidedException(String exceptionMessage){
        super(exceptionMessage);
    }
}
