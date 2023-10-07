package com.casino.replenishmentapi.repository;

import com.casino.replenishmentapi.enums.CryptoDataCurrency;
import com.casino.replenishmentapi.model.CryptoData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CryptoDataRepository extends ReactiveCrudRepository<CryptoData, Long> {
    @Query("SELECT cd.* FROM crypto_data_table cd WHERE cd.currency = :currency")
    Flux<CryptoData> findAllCryptoDataByCurrency(@Param("currency") CryptoDataCurrency currency);
}
