package com.casino.replenishments.controller;

import com.casino.replenishments.model.CryptoData;
import com.casino.replenishments.payload.CryptoDataCreatePayload;
import com.casino.replenishments.service.implement.CryptoDataServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/replenishment/cryptoData")
@RequiredArgsConstructor
public class CryptoDataController {
    private final CryptoDataServiceImpl cryptoDataService;

    @GetMapping("/getAll")
    public Flux<CryptoData> getAll(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken){
        return cryptoDataService.getAllCryptoData(Long.parseLong(usernamePasswordAuthenticationToken.getName()));
    }
    @PostMapping
    public Mono<CryptoDataCreatePayload> create(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                @RequestBody CryptoDataCreatePayload cryptoDataCreatePayload){
        return cryptoDataService.createCryptoData(Long.parseLong(usernamePasswordAuthenticationToken.getName()), cryptoDataCreatePayload);
    }

    @DeleteMapping
    public Mono<Void> delete(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                             @RequestParam("id") long id){
        return cryptoDataService.deleteCryptoData(Long.parseLong(usernamePasswordAuthenticationToken.getName()), id);
    }
}
