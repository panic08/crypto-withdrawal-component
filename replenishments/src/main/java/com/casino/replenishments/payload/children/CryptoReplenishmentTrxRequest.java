package com.casino.replenishments.payload.children;

import com.casino.replenishments.payload.CryptoReplenishmentRequest;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class CryptoReplenishmentTrxRequest extends CryptoReplenishmentRequest {
    @Range(min = 13, message = "Minimum amount for TRX replenishment - 13 TRX")
    private double amount;
}
