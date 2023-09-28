package com.casino.replenishments.service;

import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishments.model.CryptoReplenishmentSession;
import com.casino.replenishments.payload.children.CryptoReplenishmentBtcRequest;
import com.casino.replenishments.payload.children.CryptoReplenishmentEthRequest;
import com.casino.replenishments.payload.CryptoReplenishmentResponse;
import com.casino.replenishments.payload.children.CryptoReplenishmentTrxRequest;
import reactor.core.publisher.Mono;

public interface CryptoReplenishmentService {
    Mono<CryptoReplenishmentSession> getCryptoReplenishmentSession(String authorization,
                                                                   CryptoReplenishmentSessionCurrency currency);

    Mono<CryptoReplenishmentResponse> createTrxCryptoReplenishment(String authorization,
                                                                   CryptoReplenishmentTrxRequest cryptoReplenishmentTrxRequest);

    Mono<CryptoReplenishmentResponse> createEthCryptoReplenishment(String authorization,
                                                                   CryptoReplenishmentEthRequest cryptoReplenishmentEthRequest);

    Mono<CryptoReplenishmentResponse> createBtcCryptoReplenishment(String authorization,
                                                                   CryptoReplenishmentBtcRequest cryptoReplenishmentBtcRequest);
}
