package com.casino.cryptoreplenishmentprocess.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoinsDataDto {
    @JsonProperty("tron")
    private TronData tronData;
    @Getter
    @Setter
    public static class TronData{
        private double usd;
    }
}
