package com.casino.auth.controller;

import com.casino.auth.dto.PublishUserDto;
import com.casino.auth.dto.UserDto;
import com.casino.auth.service.implement.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
