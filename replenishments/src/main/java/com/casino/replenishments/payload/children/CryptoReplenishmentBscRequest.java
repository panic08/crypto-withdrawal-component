package com.casino.replenishments.payload.children;

import com.casino.replenishments.payload.CryptoReplenishmentRequest;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CryptoReplenishmentBscRequest extends CryptoReplenishmentRequest {
    @DecimalMin(value = "0.012", message = "Minimum amount for BSC replenishment - 0.012 BSC")
    private double amount;
}
