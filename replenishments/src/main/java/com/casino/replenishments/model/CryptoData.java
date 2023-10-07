package com.casino.replenishments.model;

import com.casino.replenishments.enums.CryptoDataCurrency;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CryptoData {
    private Long id;
    private String address;
    private String privateKey;
    private CryptoDataCurrency currency;
}
