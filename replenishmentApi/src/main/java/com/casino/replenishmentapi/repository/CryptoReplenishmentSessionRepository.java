package com.casino.replenishmentapi.repository;

import com.casino.replenishmentapi.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishmentapi.model.CryptoReplenishmentSession;
import reactor.core.publisher.Mono;

public interface CryptoReplenishmentSessionRepository{
    Mono<CryptoReplenishmentSession> findCryptoReplenishmentSessionByUserIdAndCurrency(long userId, CryptoReplenishmentSessionCurrency currency);

    Mono<Boolean> deleteCryptoReplenishmentSessionByUserIdAndCurrency(long userId, CryptoReplenishmentSessionCurrency currency);
    Mono<CryptoReplenishmentSession> save(CryptoReplenishmentSession cryptoReplenishmentSession);
}
