package com.casino.replenishments.model;

import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CryptoReplenishmentSession {
    private Long userId;
    private String recipientAddress;
    private Double amount;
    private CryptoReplenishmentSessionCurrency currency;
    private Long untilTimestamp;
}
