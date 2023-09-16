package com.casino.auth.handler;

import com.casino.auth.exception.FileSizeExceedsLimitException;
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
public class FileSizeExceedsLimitAdvancedHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.REQUEST_ENTITY_TOO_LARGE)
    @ExceptionHandler(FileSizeExceedsLimitException.class)
    public Mono<HashMap<Object, Object>> handleFileSizeExceedsLimitException(FileSizeExceedsLimitException fileSizeExceedsLimitException){
        return Mono.fromCallable(() -> {
            HashMap<Object, Object> hashMap = new HashMap<>();

            hashMap.put("timestamp", new Date());
            hashMap.put("status", 413);
            hashMap.put("error", "Request entity too large");
            hashMap.put("message", fileSizeExceedsLimitException.getMessage());

            return hashMap;
        });
    }
}
