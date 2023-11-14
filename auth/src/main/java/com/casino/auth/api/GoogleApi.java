package com.casino.auth.api;

import com.casino.auth.dto.google.GoogleAccessTokenDto;
import com.casino.auth.dto.google.GoogleAccessTokenRequest;
import com.casino.auth.dto.google.GoogleCertsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GoogleApi {
    private static final String  GET_GOOGLE_ACCESS_TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";
    private static final String GET_GOOGLE_CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";

    public Mono<GoogleAccessTokenDto> getGoogleAccessTokenDtoByGoogleAccessTokenRequest(GoogleAccessTokenRequest googleAccessTokenRequest){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(GET_GOOGLE_ACCESS_TOKEN_URL)
                .build()
                .post()
                .bodyValue(googleAccessTokenRequest)
                .retrieve()
                .bodyToMono(GoogleAccessTokenDto.class);
    }

    public Mono<GoogleCertsDto> getGoogleCerts(){
        return WebClient.builder()
                .baseUrl(GET_GOOGLE_CERTS_URL)
                .build()
                .get()
                .retrieve()
                .bodyToMono(GoogleCertsDto.class);
    }
}
