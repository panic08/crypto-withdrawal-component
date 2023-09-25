package com.casino.replenishments.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class WebExchangeBindAdvancedHandler {
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ServerResponse> handlerBindException(WebExchangeBindException e) {
        return Mono.error(e.getFieldError() == null ? e :
                new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        e.getFieldError().getDefaultMessage()));
    }
}
