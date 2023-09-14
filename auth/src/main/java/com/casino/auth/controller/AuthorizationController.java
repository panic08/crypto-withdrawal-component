package com.casino.auth.controller;

import com.casino.auth.dto.UserDto;
import com.casino.auth.payload.AuthorizationRequest;
import com.casino.auth.payload.AuthorizationResponse;
import com.casino.auth.service.implement.AuthorizationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/getInfoByToken")
    public Mono<UserDto> getInfoByToken(@RequestParam("token") String token){
        return authorizationService.getInfoByToken(token);
    }
}
