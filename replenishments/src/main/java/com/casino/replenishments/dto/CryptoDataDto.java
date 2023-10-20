package com.casino.replenishments.dto;

import com.casino.replenishments.enums.CryptoDataCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CryptoDataDto {
    private long id;
    private String address;
    @JsonProperty("private_key")
    private String privateKey;
    private CryptoDataCurrency currency;
}
