package com.casino.replenishments.service;

import com.casino.replenishments.dto.ReplenishmentDto;
import com.casino.replenishments.model.Replenishment;
import reactor.core.publisher.Flux;

public interface ReplenishmentService {
    Flux<ReplenishmentDto> getAllReplenishment(long userId, int startIndex, int endIndex);
}
