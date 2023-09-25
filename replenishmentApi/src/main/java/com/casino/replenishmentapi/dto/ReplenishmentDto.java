package com.casino.replenishmentapi.dto;

import com.casino.replenishmentapi.enums.ReplenishmentCurrency;
import com.casino.replenishmentapi.enums.ReplenishmentMethod;
import com.casino.replenishmentapi.model.ReplenishmentData;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReplenishmentDto {
    private Long id;
    private Long userId;
    private ReplenishmentMethod type;
    private ReplenishmentCurrency currency;
    private Double amount;
    private ReplenishmentData replenishmentData;
    private Long createdAt;
}
