package com.casino.cryptoreplenishmentprocess.service.implement;

import com.casino.cryptoreplenishmentprocess.dto.CryptoReplenishmentMessage;
import com.casino.cryptoreplenishmentprocess.service.CryptoReplenishmentService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CryptoReplenishmentServiceImpl implements CryptoReplenishmentService {
    @Override
    public Mono<Void> handleTrxCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        System.out.println("i see that town, silent hill..");
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleEthCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        System.out.println(cryptoReplenishmentMessage.getRecipientAddress() + " and price: " + cryptoReplenishmentMessage.getAmount());
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleBtcCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleUsdtTrc20CryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleUsdtErc20CryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.empty();
    }
}
