package com.casino.replenishments.model;

import com.casino.replenishments.enums.ReplenishmentCurrency;
import com.casino.replenishments.enums.ReplenishmentMethod;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Replenishment {
    private Long id;
    private Long userId;
    private ReplenishmentMethod method;
    private ReplenishmentCurrency currency;
    private Double amount;
    private Long createdAt;
}
