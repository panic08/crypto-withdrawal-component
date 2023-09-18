package com.casino.auth.service;

import com.casino.auth.payload.ChangeClientSeedRequest;
import com.casino.auth.payload.ChangeClientSeedResponse;
import com.casino.auth.payload.ChangeServerSeedResponse;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface PersonalService {
    Mono<ChangeServerSeedResponse> changeServerSeed(String authorization);
    Mono<ChangeClientSeedResponse> changeClientSeed(String authorization, ChangeClientSeedRequest changeClientSeedRequest);
    Mono<Void> changePhoto(String authorization, FilePart multipartFile);
}
