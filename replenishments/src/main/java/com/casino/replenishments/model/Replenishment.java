package com.casino.replenishments.model;

import com.casino.replenishments.enums.ReplenishmentCurrency;
import com.casino.replenishments.enums.ReplenishmentMethod;
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
