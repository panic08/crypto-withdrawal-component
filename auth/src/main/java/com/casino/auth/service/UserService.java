package com.casino.auth.service;

import com.casino.auth.dto.PublishUserDto;
import com.casino.auth.dto.UserDto;
import com.casino.auth.payload.ChangeBalancePayload;
import com.casino.auth.payload.ChangeIsAccountNonLockedPayload;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<ResponseEntity<Resource>> getUserPhotoById(long id);
    Mono<PublishUserDto> getUserById(long id);
}
