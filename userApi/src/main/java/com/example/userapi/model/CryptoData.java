package com.example.userapi.model;

import com.example.userapi.enums.CryptoDataType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cryptos_data_table")
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CryptoData {
    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("type")
    private CryptoDataType type;

    @Column("address")
    private String address;

    @Column("public_key")
    private String publicKey;

    @Column("private_key")
    private String privateKey;
}
