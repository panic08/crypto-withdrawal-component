package com.casino.auth.controller;

import com.casino.auth.payload.*;
import com.casino.auth.service.implement.PersonalServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/personal")
@RequiredArgsConstructor
public class PersonalController {

    private final PersonalServiceImpl personalService;

    @PutMapping (value = "/photo/upload", consumes = "multipart/form-data")
    public Mono<Void> changePhoto(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                  @RequestPart("photo") FilePart multipartFile){
        return personalService.changePhoto(Long.parseLong(usernamePasswordAuthenticationToken.getName()), multipartFile);
    }

    @PutMapping("/changeServerSeed")
    public Mono<ChangeServerSeedResponse> changeServerSeed(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken){
        return personalService.changeServerSeed(Long.parseLong(usernamePasswordAuthenticationToken.getName()));
    }

    //maybe Patch idk
    @PutMapping("/changeClientSeed")
    public Mono<ChangeClientSeedPayload> changeServerSeed(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                           @Valid @RequestBody ChangeClientSeedPayload changeClientSeedPayload){
        return personalService.changeClientSeed(Long.parseLong(usernamePasswordAuthenticationToken.getName()), changeClientSeedPayload);
    }

    @PutMapping("/changeProfileType")
    public Mono<ChangeProfileTypePayload> changeProfileType(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                             @RequestBody ChangeProfileTypePayload changeProfileTypePayload){
        return personalService.changeProfileType(Long.parseLong(usernamePasswordAuthenticationToken.getName()), changeProfileTypePayload);
    }

    @PutMapping("/changeBalance")
    public Mono<ChangeBalancePayload> changeBalance(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                    @Valid @RequestBody ChangeBalancePayload changeBalancePayload){
        return personalService.changeBalance(Long.parseLong(usernamePasswordAuthenticationToken.getName()), changeBalancePayload);
    }

    @PutMapping("/changeAccountNonLocked")
    public Mono<ChangeIsAccountNonLockedPayload> changeIsAccountNonLocked(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                                          @RequestBody ChangeIsAccountNonLockedPayload changeIsAccountNonLockedPayload){
        return personalService.changeIsAccountNonLocked(Long.parseLong(usernamePasswordAuthenticationToken.getName()), changeIsAccountNonLockedPayload);
    }
}
