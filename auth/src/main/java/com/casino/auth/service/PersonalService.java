package com.casino.auth.service;

import com.casino.auth.payload.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface PersonalService {
    Mono<ChangeServerSeedResponse> changeServerSeed(long principalId);
    Mono<ChangeClientSeedPayload> changeClientSeed(long principalId, ChangeClientSeedPayload changeClientSeedPayload);
    Mono<ChangeProfileTypePayload> changeProfileType(long principalId, ChangeProfileTypePayload changeProfileTypePayload);
    Mono<Void> changePhoto(long principalId, FilePart multipartFile);
    Mono<ChangeBalancePayload> changeBalance(long principalId, ChangeBalancePayload changeBalancePayload);
    Mono<ChangeIsAccountNonLockedPayload> changeIsAccountNonLocked(long principalId, ChangeIsAccountNonLockedPayload changeIsAccountNonLockedPayload);
}
