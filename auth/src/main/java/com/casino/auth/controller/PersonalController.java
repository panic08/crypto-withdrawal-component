package com.casino.auth.controller;

import com.casino.auth.payload.*;
import com.casino.auth.service.implement.PersonalServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class PersonalController {

    private final PersonalServiceImpl personalService;

    @PutMapping (value = "/photo/upload", consumes = "multipart/form-data")
    public Mono<Void> changePhoto(@RequestHeader("Authorization") String authorization,
                                  @RequestPart("photo") FilePart multipartFile){
        return personalService.changePhoto(authorization, multipartFile);
    }

    @PutMapping("/changeServerSeed")
    public Mono<ChangeServerSeedResponse> changeServerSeed(@RequestHeader("Authorization") String authorization){
        return personalService.changeServerSeed(authorization);
    }

    //maybe Patch idk
    @PutMapping("/changeClientSeed")
    public Mono<ChangeClientSeedResponse> changeServerSeed(@RequestHeader("Authorization") String authorization,
                                                           @Valid @RequestBody ChangeClientSeedRequest changeClientSeedRequest){
        return personalService.changeClientSeed(authorization, changeClientSeedRequest);
    }

    @PutMapping("/changeProfileType")
    public Mono<ChangeProfileTypeResponse> changeProfileType(@RequestHeader("Authorization") String authorization,
                                                             @RequestBody ChangeProfileTypeRequest changeProfileTypeRequest){
        return personalService.changeProfileType(authorization, changeProfileTypeRequest);
    }
}
