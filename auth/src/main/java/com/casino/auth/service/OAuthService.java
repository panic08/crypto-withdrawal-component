package com.casino.auth.service;

import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.payload.google.GoogleAuthorizationRequest;
import com.casino.auth.payload.vk.VkAuthorizationRequest;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface OAuthService {
    Mono<AuthorizationResponse> handleAuthorizeByVk(VkAuthorizationRequest vkAuthorizationRequest);
    Mono<AuthorizationResponse> handleAuthorizeByGoogle(GoogleAuthorizationRequest googleAuthorizationRequest);
    Mono<ResponseEntity<Void>> handleRedirectVK();
    Mono<ResponseEntity<Void>> handleRedirectGoogle();
}
