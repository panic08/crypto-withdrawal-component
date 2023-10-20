package com.casino.auth.controller;

import com.casino.auth.dto.UserCombinedDto;
import com.casino.auth.payload.AuthorizationRequest;
import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.service.implement.AuthorizationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationServiceImpl authorizationService;
    @PostMapping("/register")
    public Mono<AuthorizationResponse> handleRegister(@Valid
                                                          @RequestBody AuthorizationRequest authorizationRequest){
        return authorizationService.handleRegister(authorizationRequest);
    }

    @PostMapping("/login")
    public Mono<AuthorizationResponse> handleLogin(@Valid
                                                       @RequestBody AuthorizationRequest authorizationRequest){
        return authorizationService.handleLogin(authorizationRequest);
    }

    @PostMapping("/info")
    public Mono<UserCombinedDto> getUserInfoByAccessToken(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken){
        return authorizationService.getUserInfo(Long.parseLong(usernamePasswordAuthenticationToken.getName()));
    }

    @PostMapping("/refresh")
    public Mono<AuthorizationResponse> handleRefreshAccessToken(@RequestHeader("Authorization") String authorization){
        return authorizationService.handleRefreshAccessToken(authorization);
    }

}
