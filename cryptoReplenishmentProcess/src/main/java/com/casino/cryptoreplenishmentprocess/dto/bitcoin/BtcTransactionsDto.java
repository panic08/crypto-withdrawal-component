package com.casino.cryptoreplenishmentprocess.dto.bitcoin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BtcTransactionsDto {
    private String address;
    private Tx[] txs;

    @Getter
    @Setter
    public static class Tx{
        private long time;
        @JsonProperty("out")
        private Out[] outs;
    }

    @Getter
    @Setter
    public static class Out{
        private String addr;
        private long value;
    }
}
