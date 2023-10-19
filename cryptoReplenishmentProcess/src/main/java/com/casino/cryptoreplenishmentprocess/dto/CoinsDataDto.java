package com.casino.cryptoreplenishmentprocess.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoinsDataDto {
    @JsonProperty("tron")
    private PriceData tronData;
    @JsonProperty("tether")
    private PriceData tetherData;
    @JsonProperty("ethereum")
    private PriceData ethereumData;
    @JsonProperty("binancecoin")
    private PriceData binanceCoinData;
    @JsonProperty("bitcoin")
    private PriceData bitcoinData;

    @Getter
    @Setter
    public static class PriceData{
        private double rub;
    }
}
