package com.casino.replenishmentapi.repository;

import com.casino.replenishmentapi.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishmentapi.model.CryptoReplenishmentSession;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CryptoReplenishmentSessionRepository extends ReactiveCrudRepository<CryptoReplenishmentSession, Long> {
    @Query("SELECT crs.* FROM crypto_replenishment_sessions_table crs WHERE crs.user_id = :user_id AND " +
            "crs.currency = :currency")
    Mono<CryptoReplenishmentSession> findCryptoReplenishmentSessionByUserIdAndCurrency(@Param("user_id") long userId,
                                                                                       @Param("currency") CryptoReplenishmentSessionCurrency currency);

    @Query("DELETE FROM crypto_replenishment_sessions_table crs WHERE crs.user_id = :user_id AND crs.currency = :currency")
    @Modifying
    Mono<Void> deleteCryptoReplenishmentSessionByUserIdAndCurrency(@Param("user_id") long userId,
                                                                   @Param("currency") CryptoReplenishmentSessionCurrency currency);
}
