package com.casino.cryptoreplenishmentprocess.dto.tron;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TrxTransactionsDto {
    private Data[] data;
    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Data{
        @JsonProperty("ret")
        private Ret[] rets;
        @JsonProperty("txID")
        private String txId;
        private long netFee;
        @JsonProperty("blockTimestamp")
        private long blockTimestamp;
        private RawData rawData;

    }
    @Getter
    @Setter
    @ToString
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Ret{
        @JsonProperty("contractRet")
        private String contractRet;
        private long fee;
    }

    @Getter
    @Setter
    public static class RawData{
        @JsonProperty("contract")
        private Contract[] contracts;
        private long timestamp;
    }

    @Getter
    @Setter
    public static class Contract{
        private Parameter parameter;
        private String type;
    }

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Parameter{
        private Value value;
        private String typeUrl;
    }

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Value{
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String data;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private long amount;
        private String ownerAddress;
        private String toAddress;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String contractAddress;
    }
}
