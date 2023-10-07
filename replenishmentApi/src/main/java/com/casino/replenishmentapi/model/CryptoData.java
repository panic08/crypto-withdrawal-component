package com.casino.replenishmentapi.model;

import com.casino.replenishmentapi.enums.CryptoDataCurrency;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("crypto_data_table")
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CryptoData {
    @Id
    @Column("id")
    private Long id;
    @Column("address")
    private String address;
    @Column("private_key")
    private String privateKey;
    @Column("currency")
    private CryptoDataCurrency currency;
}
