package com.casino.replenishments.controller;

import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishments.model.CryptoReplenishmentSession;
import com.casino.replenishments.payload.children.*;
import com.casino.replenishments.payload.CryptoReplenishmentResponse;
import com.casino.replenishments.service.implement.CryptoReplenishmentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/replenishment/crypto")
@RequiredArgsConstructor
public class CryptoReplenishmentController {

    private final CryptoReplenishmentServiceImpl cryptoReplenishmentService;
    @GetMapping
    public Mono<CryptoReplenishmentSession> get(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                @RequestParam("currency") CryptoReplenishmentSessionCurrency currency){
        return cryptoReplenishmentService.getCryptoReplenishmentSession(Long.parseLong(usernamePasswordAuthenticationToken.getName()), currency);
    }

    @DeleteMapping
    public Mono<Void> delete(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                             @RequestParam("currency") CryptoReplenishmentSessionCurrency currency){
        return cryptoReplenishmentService.deleteCryptoReplenishmentSession(Long.parseLong(usernamePasswordAuthenticationToken.getName()), currency);
    }

    @PostMapping("/createTrx")
    public Mono<CryptoReplenishmentResponse> createTrx(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                       @Valid @RequestBody CryptoReplenishmentTrxRequest cryptoReplenishmentTrxRequest){
        return cryptoReplenishmentService.createTrxCryptoReplenishment(Long.parseLong(usernamePasswordAuthenticationToken.getName()), cryptoReplenishmentTrxRequest);
    }

    @PostMapping("/createEth")
    public Mono<CryptoReplenishmentResponse> createEth(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                       @Valid @RequestBody CryptoReplenishmentEthRequest cryptoReplenishmentEthRequest){

        return cryptoReplenishmentService.createEthCryptoReplenishment(Long.parseLong(usernamePasswordAuthenticationToken.getName()), cryptoReplenishmentEthRequest);
    }

    @PostMapping("/createBtc")
    public Mono<CryptoReplenishmentResponse> createBtc(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                       @Valid @RequestBody CryptoReplenishmentBtcRequest cryptoReplenishmentBtcRequest){
        return cryptoReplenishmentService.createBtcCryptoReplenishment(Long.parseLong(usernamePasswordAuthenticationToken.getName()), cryptoReplenishmentBtcRequest);
    }

    @PostMapping("/createUsdtTRC20")
    public Mono<CryptoReplenishmentResponse> createUsdtTrc20(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                             @Valid @RequestBody CryptoReplenishmentUsdtRequest cryptoReplenishmentUsdtRequest){
        return cryptoReplenishmentService.createUsdtTrc20CryptoReplenishment(Long.parseLong(usernamePasswordAuthenticationToken.getName()), cryptoReplenishmentUsdtRequest);
    }

    @PostMapping("/createBsc")
    public Mono<CryptoReplenishmentResponse> createBsc(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                                       @Valid @RequestBody CryptoReplenishmentBscRequest cryptoReplenishmentBscRequest){
        return cryptoReplenishmentService.createBscCryptoReplenishment(Long.parseLong(usernamePasswordAuthenticationToken.getName()), cryptoReplenishmentBscRequest);
    }
}
