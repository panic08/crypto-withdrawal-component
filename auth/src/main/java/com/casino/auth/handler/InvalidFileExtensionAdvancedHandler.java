package com.casino.auth.handler;

import com.casino.auth.exception.InvalidFileExtensionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;

@RestControllerAdvice
public class InvalidFileExtensionAdvancedHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidFileExtensionException.class)
    public Mono<HashMap<Object, Object>> handleInvalidCredentialsException(InvalidFileExtensionException invalidFileExtensionException){
        return Mono.fromCallable(() -> {
            HashMap<Object, Object> hashMap = new HashMap<>();

            hashMap.put("timestamp", new Date());
            hashMap.put("status", 400);
            hashMap.put("error", "Conflict");
            hashMap.put("message", invalidFileExtensionException.getMessage());

            return hashMap;
        });
    }
}
