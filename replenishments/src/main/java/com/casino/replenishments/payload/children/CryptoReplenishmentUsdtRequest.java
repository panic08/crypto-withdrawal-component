package com.casino.replenishments.payload.children;

import com.casino.replenishments.payload.CryptoReplenishmentRequest;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CryptoReplenishmentUsdtRequest extends CryptoReplenishmentRequest {
    @DecimalMin(value = "1.1", message = "Minimum amount for USDT replenishment - 1.1 USDT")
    private double amount;
}
