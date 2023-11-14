package com.casino.auth.api;

import com.casino.auth.dto.steam.SteamUserDto;
import com.casino.auth.payload.steam.SteamAuthorizationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SteamApi {
    private static final String GET_STEAM_USER_URL = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2";
    private static final String AUTHENTICATE_STEAM_USER_URL = "https://steamcommunity.com/openid/login";

    public Mono<SteamUserDto> getSteamUserBySteamid(String clientSecret, String steamid){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(GET_STEAM_USER_URL + "?key="
                        + clientSecret
                        + "&steamids="
                        + steamid)
                .build()
                .get()
                .retrieve()
                .bodyToMono(SteamUserDto.class);
    }

    public Mono<String> authenticateSteamUser(SteamAuthorizationRequest steamAuthorizationRequest){
        WebClient.Builder webClient = WebClient.builder();

        return webClient.baseUrl(URLDecoder.decode(AUTHENTICATE_STEAM_USER_URL + "?openid.ns="
                        + steamAuthorizationRequest.getOpenidNs()
                        + "&openid.mode=check_authentication"
                        + "&openid.op_endpoint="
                        + steamAuthorizationRequest.getOpenidOpEndpoint()
                        + "&openid.claimed_id="
                        + steamAuthorizationRequest.getOpenidClaimedId()
                        + "&openid.identity="
                        + steamAuthorizationRequest.getOpenidIdentity()
                        + "&openid.return_to="
                        + steamAuthorizationRequest.getOpenidReturnTo()
                        + "&openid.response_nonce="
                        + steamAuthorizationRequest.getOpenidResponseNonce()
                        + "&openid.assoc_handle="
                        + steamAuthorizationRequest.getOpenidAssocHandle()
                        + "&openid.signed="
                        + steamAuthorizationRequest.getOpenidSigned()
                        + "&openid.sig="
                        + steamAuthorizationRequest.getOpenidSig(), StandardCharsets.UTF_8))
                .build()
                .get()
                .retrieve()
                .bodyToMono(String.class);
    }
}
