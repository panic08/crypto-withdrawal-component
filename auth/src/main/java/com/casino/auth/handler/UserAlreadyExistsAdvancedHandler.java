package com.casino.auth.handler;

import com.casino.auth.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;

@RestControllerAdvice
public class UserAlreadyExistsAdvancedHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public Mono<HashMap<Object, Object>> handleUserAlreadyExistsException(UserAlreadyExistsException userAlreadyExistsException){
        return Mono.fromCallable(() -> {
            HashMap<Object, Object> hashMap = new HashMap<>();

            hashMap.put("timestamp", new Date());
            hashMap.put("status", 409);
            hashMap.put("error", "Conflict");
            hashMap.put("message", userAlreadyExistsException.getMessage());

            return hashMap;
        });
    }
}
