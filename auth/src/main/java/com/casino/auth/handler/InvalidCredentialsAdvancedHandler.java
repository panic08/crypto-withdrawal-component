package com.casino.auth.handler;

import com.casino.auth.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;

@RestControllerAdvice
public class InvalidCredentialsAdvancedHandler  {
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<Void> handleInvalidCredentialsException(InvalidCredentialsException invalidCredentialsException){
        return Mono.empty();
    }
}
