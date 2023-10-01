package com.casino.replenishments.payload.children;

import com.casino.replenishments.payload.CryptoReplenishmentRequest;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CryptoReplenishmentUsdtRequest extends CryptoReplenishmentRequest {
    @DecimalMin(value = "3.3", message = "Minimum amount for USDT replenishment - 3.3 USDT")
    private double amount;
}
