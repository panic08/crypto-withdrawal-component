package com.casino.auth.controller;

import com.casino.auth.dto.PublicUserCombinedDto;
import com.casino.auth.service.implement.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping("/{id}")
    public Mono<PublicUserCombinedDto> get(@PathVariable("id") long id){
        return userService.getUserById(id);
    }

    @GetMapping("/photo/{id}")
    public Mono<ResponseEntity<Resource>> getPhoto(@PathVariable("id") long id){
        return userService.getUserPhotoById(id);
    }
}
