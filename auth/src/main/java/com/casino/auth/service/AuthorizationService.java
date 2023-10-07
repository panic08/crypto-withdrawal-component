package com.casino.auth.service;

import com.casino.auth.dto.UserDto;
import com.casino.auth.payload.AuthorizationRequest;
import com.casino.auth.payload.AuthorizationResponse;
import reactor.core.publisher.Mono;

public interface AuthorizationService {
    Mono<AuthorizationResponse> handleRegister(AuthorizationRequest authorizationRequest);
    Mono<AuthorizationResponse> handleLogin(AuthorizationRequest authorizationRequest);
    Mono<UserDto> getUserInfoByAccessToken(String authorization);
    Mono<AuthorizationResponse> handleRefreshAccessToken(String authorization);
}
