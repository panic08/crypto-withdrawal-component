package com.casino.replenishments.payload;

import com.casino.replenishments.enums.CryptoDataCurrency;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CryptoDataCreatePayload {
    private String address;
    private String privateKey;
    private CryptoDataCurrency currency;
}
