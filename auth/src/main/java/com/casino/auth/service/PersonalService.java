package com.casino.auth.service;

import com.casino.auth.payload.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface PersonalService {
    Mono<ChangeServerSeedResponse> changeServerSeed(long id);
    Mono<ChangeClientSeedPayload> changeClientSeed(long id, ChangeClientSeedPayload changeClientSeedPayload);
    Mono<ChangeProfileTypePayload> changeProfileType(long id, ChangeProfileTypePayload changeProfileTypePayload);
    Mono<Void> changePhoto(long id, FilePart multipartFile);
    Mono<ChangeBalancePayload> changeBalance(long id, ChangeBalancePayload changeBalancePayload);
    Mono<ChangeIsAccountNonLockedPayload> changeIsAccountNonLocked(long id, ChangeIsAccountNonLockedPayload changeIsAccountNonLockedPayload);
}
