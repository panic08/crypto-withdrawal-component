package com.casino.auth.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("steam.oauth")
@Getter
@Setter
public class SteamOAuthProperty {
    private String redirectUrl;
    private String clientSecret;
}
