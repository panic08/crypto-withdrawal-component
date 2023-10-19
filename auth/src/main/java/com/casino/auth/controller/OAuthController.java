package com.casino.auth.controller;

import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.payload.google.GoogleAuthorizationRequest;
import com.casino.auth.payload.steam.SteamAuthorizationRequest;
import com.casino.auth.payload.vk.VkAuthorizationRequest;
import com.casino.auth.service.implement.OAuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthServiceImpl oAuthService;

    @GetMapping("/vk")
    public Mono<ResponseEntity<Void>> redirectVK(){
        return oAuthService.handleRedirectVK();
    }

    @GetMapping("/google")
    public Mono<ResponseEntity<Void>> redirectGoogle(){
        return oAuthService.handleRedirectGoogle();
    }

    @GetMapping("/steam")
    public Mono<ResponseEntity<Void>> redirectSteam(){
        return oAuthService.handleRedirectSteam();
    }

    @PostMapping("/authorizeByVk")
    public Mono<AuthorizationResponse> handleAuthorizeByVk(@Valid
                                                                @RequestBody VkAuthorizationRequest vkAuthorizationRequest){
        return oAuthService.handleAuthorizeByVk(vkAuthorizationRequest);
    }

    @PostMapping("/authorizeByGoogle")
    public Mono<AuthorizationResponse> handleAuthorizeByGoogle(@Valid
                                                               @RequestBody GoogleAuthorizationRequest googleAuthorizationRequest){
        return oAuthService.handleAuthorizeByGoogle(googleAuthorizationRequest);
    }

    @PostMapping("/authorizeBySteam")
    public Mono<AuthorizationResponse> handleAuthorizeBySteam(@Valid
                                                              @RequestBody SteamAuthorizationRequest steamAuthorizationRequest){
        return oAuthService.handleAuthorizeBySteam(steamAuthorizationRequest);
    }
}
