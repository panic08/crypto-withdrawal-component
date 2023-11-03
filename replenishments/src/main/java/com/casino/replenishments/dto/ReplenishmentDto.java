package com.casino.replenishments.dto;

import com.casino.replenishments.enums.ReplenishmentCurrency;
import com.casino.replenishments.enums.ReplenishmentMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReplenishmentDto {
    private long id;
    private ReplenishmentMethod method;
    private ReplenishmentCurrency currency;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserCombinedDto user;
    private double amount;
    private long createdAt;
}
