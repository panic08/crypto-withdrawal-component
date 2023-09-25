package com.casino.replenishments.handler;

import com.casino.replenishments.exception.CryptoReplenishmentExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;

@RestControllerAdvice
public class CryptoReplenishmentExistsAdvancedHandler {
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CryptoReplenishmentExistsException.class)
    public Mono<HashMap<Object, Object>> handleCryptoReplenishmentExists(CryptoReplenishmentExistsException cryptoReplenishmentExistsException){
        return Mono.fromCallable(() -> {
            HashMap<Object, Object> hashMap = new HashMap<>();

            hashMap.put("timestamp", new Date());
            hashMap.put("status", 409);
            hashMap.put("error", "Conflict");
            hashMap.put("message", cryptoReplenishmentExistsException.getMessage());

            return hashMap;
        });
    }
}
