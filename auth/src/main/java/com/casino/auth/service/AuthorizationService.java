package com.casino.auth.service;

import com.casino.auth.dto.UserCombinedDto;
import com.casino.auth.payload.AuthorizationRequest;
import com.casino.auth.payload.AuthorizationResponse;
import reactor.core.publisher.Mono;

public interface AuthorizationService {
    Mono<AuthorizationResponse> handleRegister(AuthorizationRequest authorizationRequest);
    Mono<AuthorizationResponse> handleLogin(AuthorizationRequest authorizationRequest);
    Mono<UserCombinedDto> getUserInfo(long id);
    Mono<AuthorizationResponse> handleRefreshAccessToken(String authorization);
}
