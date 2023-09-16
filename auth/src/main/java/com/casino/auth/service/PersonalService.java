package com.casino.auth.service;

import com.casino.auth.payload.ChangeClientSeedRequest;
import com.casino.auth.payload.ChangeClientSeedResponse;
import com.casino.auth.payload.ChangeServerSeedResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface PersonalService {
    Mono<ChangeServerSeedResponse> changeServerSeed(String authorization);
    Mono<ChangeClientSeedResponse> changeClientSeed(String authorization, ChangeClientSeedRequest changeClientSeedRequest);
    Mono<ResponseEntity<Resource>> getPhoto(long id);
    Mono<Void> uploadPhoto(String authorization, FilePart multipartFile);
}
