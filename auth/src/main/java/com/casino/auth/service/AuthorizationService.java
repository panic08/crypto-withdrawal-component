package com.casino.auth.service;

import com.casino.auth.dto.UserDto;
import com.casino.auth.payload.AuthorizationRequest;
import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.payload.vk.VkAuthorizationRequest;
import reactor.core.publisher.Mono;

public interface AuthorizationService {
    Mono<AuthorizationResponse> handleRegister(AuthorizationRequest authorizationRequest);
    Mono<AuthorizationResponse> handleLogin(AuthorizationRequest authorizationRequest);
    Mono<UserDto> getInfoByToken(String token);
}
