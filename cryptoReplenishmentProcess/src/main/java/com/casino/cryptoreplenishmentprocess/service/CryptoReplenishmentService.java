package com.casino.cryptoreplenishmentprocess.service;

import com.casino.cryptoreplenishmentprocess.dto.CryptoReplenishmentMessage;
import reactor.core.publisher.Mono;

public interface CryptoReplenishmentService {
    Mono<Void> handleTrxCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage);
    Mono<Void> handleEthCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage);
    Mono<Void> handleBtcCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage);
    Mono<Void> handleUsdtTrc20CryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage);
    Mono<Void> handleBscCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage);
}
