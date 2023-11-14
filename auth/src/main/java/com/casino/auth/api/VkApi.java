package com.casino.auth.api;

import com.casino.auth.dto.vk.VkAccessTokenDto;
import com.casino.auth.dto.vk.VkUserDto;
import com.casino.auth.property.VkOAuthProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class VkApi {
    private static final String GET_VK_USER_URL = "https://api.vk.com/method/users.get";
    private static final String GET_VK_ACCESS_CODE_URL = "https://oauth.vk.com/access_token";

    public Mono<VkAccessTokenDto> getVKAccessTokenByCode(
            String clientId,
            String clientSecret,
            String redirectUrl,
            String code
    ){
        WebClient.Builder webClient = WebClient.builder();
        return webClient
                .baseUrl(GET_VK_ACCESS_CODE_URL + "?client_id="
                        + clientId
                        + "&client_secret=" + clientSecret
                        + "&redirect_uri=" + redirectUrl
                        + "&code=" + code
                )
                .build()
                .post()
                .retrieve()
                .bodyToMono(VkAccessTokenDto.class);
    }

    public Mono<VkUserDto> getVKUserByAccessToken(String accessToken){
        return WebClient.builder()
                .baseUrl(GET_VK_USER_URL + "?v=5.131&fields=has_photo,photo_max")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build()
                .get()
                .retrieve()
                .bodyToMono(VkUserDto.class)
                .cache();
    }
}
