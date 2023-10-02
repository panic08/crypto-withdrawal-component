package com.casino.replenishmentapi.repository.implement;

import com.casino.replenishmentapi.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishmentapi.model.CryptoReplenishmentSession;
import com.casino.replenishmentapi.repository.CryptoReplenishmentSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CryptoReplenishmentSessionRepositoryImpl implements CryptoReplenishmentSessionRepository {

    private final ReactiveRedisTemplate<String, CryptoReplenishmentSession> reactiveRedisTemplate;
    @Override
    public Mono<CryptoReplenishmentSession> findCryptoReplenishmentSessionByUserIdAndCurrency(long userId, CryptoReplenishmentSessionCurrency currency) {
        return reactiveRedisTemplate.opsForValue()
                .get("crypto_replenishment_session:user_id:" + userId + ":currency:" + currency);
    }

    @Override
    public Mono<Boolean> deleteCryptoReplenishmentSessionByUserIdAndCurrency(long userId, CryptoReplenishmentSessionCurrency currency) {
        return reactiveRedisTemplate.opsForValue()
                .delete("crypto_replenishment_session:user_id:" + userId + ":currency:" + currency);
    }

    @Override
    public Mono<CryptoReplenishmentSession> save(CryptoReplenishmentSession cryptoReplenishmentSession) {
        return reactiveRedisTemplate.opsForValue()
                .set("crypto_replenishment_session:user_id:" + cryptoReplenishmentSession.getUserId()
                                + ":currency:" + cryptoReplenishmentSession.getCurrency(),
                        cryptoReplenishmentSession)
                .thenReturn(cryptoReplenishmentSession);
    }
}
