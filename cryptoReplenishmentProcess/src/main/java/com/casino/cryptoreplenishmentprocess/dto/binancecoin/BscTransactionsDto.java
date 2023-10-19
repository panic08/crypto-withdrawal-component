package com.casino.cryptoreplenishmentprocess.dto.binancecoin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BscTransactionsDto {
    private String status;
    @JsonProperty("results")
    private Result[] results;

    @Getter
    @Setter
    public static class Result{
        @JsonProperty("timeStamp")
        private String timestamp;
        private String from;
        private String to;
        private String value;
    }
}
