package com.casino.cryptoreplenishmentprocess.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("com.casino.crypto-providers-api-keys")
@Getter
@Setter
public class CryptoProvidersApiKeysProperty {
    private String etherScan;
    private String bscScan;
}
