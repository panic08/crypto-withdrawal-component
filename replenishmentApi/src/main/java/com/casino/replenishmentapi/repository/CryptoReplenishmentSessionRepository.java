package com.casino.replenishmentapi.repository;

import com.casino.replenishmentapi.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishmentapi.model.CryptoReplenishmentSession;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CryptoReplenishmentSessionRepository extends ReactiveCrudRepository<CryptoReplenishmentSession, Long> {
    @Query("SELECT cr.* FROM crypto_replenishment_sessions_table cr WHERE cr.user_id = :user_id AND " +
            "cr.currency = :currency")
    Mono<CryptoReplenishmentSession> findCryptoReplenishmentSessionByUserIdAndCurrency(@Param("user_id") long userId,
                                                                                       @Param("currency") CryptoReplenishmentSessionCurrency currency);
}
