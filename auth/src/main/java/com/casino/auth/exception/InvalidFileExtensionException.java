package com.casino.auth.exception;

public class InvalidFileExtensionException extends RuntimeException{
    public InvalidFileExtensionException(String exceptionMessage){
        super(exceptionMessage);
    }
}
