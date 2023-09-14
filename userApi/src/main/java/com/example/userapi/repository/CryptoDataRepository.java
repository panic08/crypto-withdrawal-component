package com.example.userapi.repository;

import com.example.userapi.model.CryptoData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CryptoDataRepository extends ReactiveCrudRepository<CryptoData, Long> {
    @Query("SELECT DISTINCT c.* FROM cryptos_data_table c WHERE c.user_id = :user_id")
    Flux<CryptoData> findAllByUserId(@Param("user_id") Long userId);
}
