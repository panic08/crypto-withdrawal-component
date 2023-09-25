package com.casino.replenishments.exception;

public class CryptoReplenishmentExistsException extends RuntimeException{
    public CryptoReplenishmentExistsException(String exceptionMessage){
        super(exceptionMessage);
    }
}
