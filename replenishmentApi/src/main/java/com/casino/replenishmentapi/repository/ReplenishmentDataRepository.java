package com.casino.replenishmentapi.repository;

import com.casino.replenishmentapi.model.ReplenishmentData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplenishmentDataRepository extends ReactiveCrudRepository<ReplenishmentData, Long> {
}
