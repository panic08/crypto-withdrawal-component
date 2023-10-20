package com.casino.replenishments.dto;

import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CryptoReplenishmentSessionDto {
    private String recipientAddress;
    private double amount;
    private CryptoReplenishmentSessionCurrency currency;
    private long untilTimestamp;
}
