package com.casino.cryptoreplenishmentprocess.model;

import com.casino.cryptoreplenishmentprocess.enums.ReplenishmentCurrency;
import com.casino.cryptoreplenishmentprocess.enums.ReplenishmentMethod;
import lombok.Data;

@Data
public class Replenishment {
    private Long id;
    private Long userId;
    private ReplenishmentMethod method;
    private ReplenishmentCurrency currency;
    private Double amount;
    private Long createdAt;
}
