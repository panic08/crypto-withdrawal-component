package com.casino.auth.controller;

import com.casino.auth.dto.PublishUserDto;
import com.casino.auth.payload.ChangeBalancePayload;
import com.casino.auth.payload.ChangeIsAccountNonLockedPayload;
import com.casino.auth.service.implement.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping("/{id}")
    public Mono<PublishUserDto> get(@PathVariable("id") long id){
        return userService.getUserById(id);
    }

    @GetMapping("/photo/{id}")
    public Mono<ResponseEntity<Resource>> getPhoto(@PathVariable("id") long id){
        return userService.getUserPhotoById(id);
    }
}
