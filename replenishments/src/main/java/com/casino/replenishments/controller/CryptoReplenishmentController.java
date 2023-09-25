package com.casino.replenishments.controller;

import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishments.model.CryptoReplenishmentSession;
import com.casino.replenishments.payload.CryptoReplenishmentEthRequest;
import com.casino.replenishments.payload.CryptoReplenishmentResponse;
import com.casino.replenishments.payload.CryptoReplenishmentTrxRequest;
import com.casino.replenishments.service.implement.CryptoReplenishmentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/replenishment/crypto")
@RequiredArgsConstructor
public class CryptoReplenishmentController {

    private final CryptoReplenishmentServiceImpl cryptoReplenishmentService;
    @GetMapping
    public Mono<CryptoReplenishmentSession> get(@RequestHeader("Authorization") String authorization,
                                                @RequestParam("currency") CryptoReplenishmentSessionCurrency currency){
        return cryptoReplenishmentService.getCryptoReplenishmentSession(authorization, currency);
    }

    @PostMapping("/createTrx")
    public Mono<CryptoReplenishmentResponse> createTrx(@RequestHeader("Authorization") String authorization,
                                                       @Valid @RequestBody CryptoReplenishmentTrxRequest cryptoReplenishmentTrxRequest){
        return cryptoReplenishmentService.createTrxCryptoReplenishment(authorization, cryptoReplenishmentTrxRequest);
    }

    @PostMapping("/createEth")
    public Mono<CryptoReplenishmentResponse> createEth(@RequestHeader("Authorization") String authorization,
                                                       @Valid @RequestBody CryptoReplenishmentEthRequest cryptoReplenishmentEthRequest){
        return cryptoReplenishmentService.createEthCryptoReplenishment(authorization, cryptoReplenishmentEthRequest);
    }
}
