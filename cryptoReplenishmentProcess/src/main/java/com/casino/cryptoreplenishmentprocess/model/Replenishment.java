package com.casino.cryptoreplenishmentprocess.model;

import com.casino.cryptoreplenishmentprocess.enums.ReplenishmentCurrency;
import com.casino.cryptoreplenishmentprocess.enums.ReplenishmentMethod;
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
