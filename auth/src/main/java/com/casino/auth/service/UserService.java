package com.casino.auth.service;

import com.casino.auth.dto.PublicUserCombinedDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<ResponseEntity<Resource>> getUserPhotoById(long id);
    Mono<PublicUserCombinedDto> getUserById(long id);
}
