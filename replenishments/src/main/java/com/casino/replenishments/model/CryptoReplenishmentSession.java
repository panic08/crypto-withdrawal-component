package com.casino.replenishments.model;

import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import lombok.Data;

@Data
public class CryptoReplenishmentSession {
    private Long id;
    private Long userId;
    private String recipientAddress;
    private Double amount;
    private CryptoReplenishmentSessionCurrency currency;
    private Long untilTimestamp;
}
