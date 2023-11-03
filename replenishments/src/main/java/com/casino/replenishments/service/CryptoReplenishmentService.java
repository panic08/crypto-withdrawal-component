package com.casino.replenishments.service;

import com.casino.replenishments.dto.CryptoReplenishmentSessionDto;
import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishments.payload.children.*;
import reactor.core.publisher.Mono;

public interface CryptoReplenishmentService {
    Mono<CryptoReplenishmentSessionDto> getCryptoReplenishmentSession(long principalId,
                                                                      CryptoReplenishmentSessionCurrency currency);

    Mono<Void> deleteCryptoReplenishmentSession(long principalId,
                                                CryptoReplenishmentSessionCurrency currency);

    Mono<CryptoReplenishmentSessionDto> createTrxCryptoReplenishment(long principalId,
                                                                   CryptoReplenishmentTrxRequest cryptoReplenishmentTrxRequest);

    Mono<CryptoReplenishmentSessionDto> createEthCryptoReplenishment(long principalId,
                                                                   CryptoReplenishmentEthRequest cryptoReplenishmentEthRequest);

    Mono<CryptoReplenishmentSessionDto> createBtcCryptoReplenishment(long principalId,
                                                                   CryptoReplenishmentBtcRequest cryptoReplenishmentBtcRequest);

    Mono<CryptoReplenishmentSessionDto> createUsdtTrc20CryptoReplenishment(long principalId,
                                                                           CryptoReplenishmentUsdtRequest cryptoReplenishmentUsdtRequest);

    Mono<CryptoReplenishmentSessionDto> createBscCryptoReplenishment(long principalId,
                                                                   CryptoReplenishmentBscRequest cryptoReplenishmentBscRequest);
}
