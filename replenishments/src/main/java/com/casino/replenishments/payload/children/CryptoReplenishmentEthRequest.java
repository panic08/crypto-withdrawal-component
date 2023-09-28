package com.casino.replenishments.payload.children;

import com.casino.replenishments.payload.CryptoReplenishmentRequest;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CryptoReplenishmentEthRequest extends CryptoReplenishmentRequest {
    @DecimalMin(value = "0.002", message = "Minimum amount for ETH replenishment - 0.002 ETH")
    private double amount;
}
