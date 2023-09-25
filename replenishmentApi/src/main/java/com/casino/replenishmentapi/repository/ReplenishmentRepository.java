package com.casino.replenishmentapi.repository;

import com.casino.replenishmentapi.model.Replenishment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReplenishmentRepository extends ReactiveCrudRepository<Replenishment, Long> {
    @Query("SELECT r.* FROM replenishments_table r WHERE r.user_id = :user_id ORDER BY r.created_at" +
            " DESC LIMIT :end_index OFFSET :start_index")
    Flux<Replenishment> findAllByUserIdByCreatedAtDesc(@Param("user_id") long userId,
                                                       @Param("start_index") int startIndex,
                                                       @Param("end_index") int endIndex);
}
