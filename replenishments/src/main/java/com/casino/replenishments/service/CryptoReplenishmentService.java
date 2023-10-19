package com.casino.replenishments.service;

import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishments.model.CryptoReplenishmentSession;
import com.casino.replenishments.payload.children.*;
import com.casino.replenishments.payload.CryptoReplenishmentResponse;
import reactor.core.publisher.Mono;

public interface CryptoReplenishmentService {
    Mono<CryptoReplenishmentSession> getCryptoReplenishmentSession(long userId,
                                                                   CryptoReplenishmentSessionCurrency currency);

    Mono<Void> deleteCryptoReplenishmentSession(long userId,
                                                CryptoReplenishmentSessionCurrency currency);

    Mono<CryptoReplenishmentResponse> createTrxCryptoReplenishment(long userId,
                                                                   CryptoReplenishmentTrxRequest cryptoReplenishmentTrxRequest);

    Mono<CryptoReplenishmentResponse> createEthCryptoReplenishment(long userId,
                                                                   CryptoReplenishmentEthRequest cryptoReplenishmentEthRequest);

    Mono<CryptoReplenishmentResponse> createBtcCryptoReplenishment(long userId,
                                                                   CryptoReplenishmentBtcRequest cryptoReplenishmentBtcRequest);

    Mono<CryptoReplenishmentResponse> createUsdtTrc20CryptoReplenishment(long userId,
                                                                           CryptoReplenishmentUsdtRequest cryptoReplenishmentUsdtRequest);

    Mono<CryptoReplenishmentResponse> createBscCryptoReplenishment(long userId,
                                                                   CryptoReplenishmentBscRequest cryptoReplenishmentBscRequest);
}
