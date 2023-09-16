package com.casino.auth.controller;

import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.payload.vk.VkAuthorizationRequest;
import com.casino.auth.property.VkOAuthProperties;
import com.casino.auth.service.implement.OAuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthServiceImpl oAuthService;

    @GetMapping("/vk")
    public Mono<ResponseEntity<Void>> redirectVK(){
        return oAuthService.handleRedirectVK();
    }

    @PostMapping("/authorizeByVk")
    public Mono<AuthorizationResponse> handleAuthorizeByVk(@Valid
                                                                @RequestBody VkAuthorizationRequest vkAuthorizationRequest){
        return oAuthService.handleAuthorizeByVk(vkAuthorizationRequest);
    }
}
