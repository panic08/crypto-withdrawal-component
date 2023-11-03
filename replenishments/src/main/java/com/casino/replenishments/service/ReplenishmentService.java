package com.casino.replenishments.service;

import com.casino.replenishments.dto.ReplenishmentDto;
import com.casino.replenishments.model.Replenishment;
import reactor.core.publisher.Flux;

public interface ReplenishmentService {
    Flux<ReplenishmentDto> getAllReplenishment(long principalId, int startIndex, int endIndex);
    Flux<ReplenishmentDto> getAllReplenishmentForRole(long principalId, int startIndex, int endIndex);
    Flux<ReplenishmentDto> getAllReplenishmentByUserId(long principalId, long userId, int startIndex, int endIndex);
}
