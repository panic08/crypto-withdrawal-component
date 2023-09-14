package com.casino.auth.model;

import com.casino.auth.enums.CryptoDataType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CryptoData {
    private Long id;
    private Long userId;
    private CryptoDataType type;
    private String address;
    private String publicKey;
    private String privateKey;
}
