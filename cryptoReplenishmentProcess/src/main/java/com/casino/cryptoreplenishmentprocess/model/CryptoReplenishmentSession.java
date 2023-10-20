package com.casino.cryptoreplenishmentprocess.model;

import com.casino.cryptoreplenishmentprocess.enums.CryptoReplenishmentSessionCurrency;
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
