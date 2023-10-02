package com.casino.replenishmentapi.model;

import com.casino.replenishmentapi.enums.CryptoReplenishmentSessionCurrency;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CryptoReplenishmentSession {
    @Column("user_id")
    private Long userId;
    @Column("recipient_address")
    private String recipientAddress;
    @Column("amount")
    private Double amount;
    @Column("currency")
    private CryptoReplenishmentSessionCurrency currency;
    @Column("until_timestamp")
    private Long untilTimestamp;
}
