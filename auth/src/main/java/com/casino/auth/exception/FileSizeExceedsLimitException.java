package com.casino.auth.exception;

public class FileSizeExceedsLimitException extends RuntimeException{
    public FileSizeExceedsLimitException(String exceptionMessage){
        super(exceptionMessage);
    }
}
