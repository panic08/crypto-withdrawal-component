package com.casino.auth.service;

import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.payload.vk.VkAuthorizationRequest;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface OAuthService {
    Mono<AuthorizationResponse> handleAuthorizeByVk(VkAuthorizationRequest vkAuthorizationRequest);
    Mono<ResponseEntity<Void>> handleRedirectVK();
}
