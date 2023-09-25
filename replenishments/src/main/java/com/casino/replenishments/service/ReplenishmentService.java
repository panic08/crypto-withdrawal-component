package com.casino.replenishments.service;

import com.casino.replenishments.model.Replenishment;
import reactor.core.publisher.Flux;

public interface ReplenishmentService {
    Flux<Replenishment> getAllReplenishment(String authorization, int startIndex, int endIndex);
}
