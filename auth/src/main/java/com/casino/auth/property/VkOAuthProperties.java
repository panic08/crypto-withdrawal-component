package com.casino.auth.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("vk.oauth")
@Getter
@Setter
public class VkOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String scopes;
}
