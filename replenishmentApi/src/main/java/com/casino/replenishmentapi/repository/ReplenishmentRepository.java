package com.casino.replenishmentapi.repository;

import com.casino.replenishmentapi.model.Replenishment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReplenishmentRepository extends ReactiveCrudRepository<Replenishment, Long> {
    @Query("SELECT r.* FROM replenishments_table r WHERE user_id = :userId  ORDER BY r.created_at LIMIT :limit")
    Flux<Replenishment> findAllByUserIdByCreatedAtDesc(@Param("userId") long userId,
                                                       @Param("limit") int limit);
}
