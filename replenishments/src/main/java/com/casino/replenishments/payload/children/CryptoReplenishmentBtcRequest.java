package com.casino.replenishments.payload.children;

import com.casino.replenishments.payload.CryptoReplenishmentRequest;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CryptoReplenishmentBtcRequest extends CryptoReplenishmentRequest {
    @DecimalMin(value = "0.000121", message = "Minimum amount for BTC replenishment - 0.000121 BTC")
    private double amount;
}
