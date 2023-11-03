package com.casino.cryptoreplenishmentprocess.api;

import com.casino.cryptoreplenishmentprocess.enums.CryptoReplenishmentSessionCurrency;
import com.casino.cryptoreplenishmentprocess.model.CryptoReplenishmentSession;
import com.casino.cryptoreplenishmentprocess.model.Replenishment;
import com.casino.cryptoreplenishmentprocess.property.ServicesIpProperty;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReplenishmentApi {
    private static String FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static String DELETE_CRYPTO_REPLENISHMENT_SESSION_URL;
    private static String SAVE_REPLENISHMENT_URL;

    private final ServicesIpProperty servicesIpProperty;
    private final WebClient.Builder webClient;

    @PostConstruct
    public void init() {
        FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/findCryptoReplenishmentSessionByUserIdAndCurrency";
        DELETE_CRYPTO_REPLENISHMENT_SESSION_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/deleteByUserIdAndCurrency";
        SAVE_REPLENISHMENT_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/replenishment/save";
    }

    public Mono<Replenishment> saveReplenishment(Replenishment replenishment){
        return webClient
                .baseUrl(SAVE_REPLENISHMENT_URL)
                .build()
                .post()
                .bodyValue(replenishment)
                .retrieve()
                .bodyToMono(Replenishment.class)
                .cache();
    }


    public Mono<Void> deleteCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                                           CryptoReplenishmentSessionCurrency currency){

        return webClient.baseUrl(DELETE_CRYPTO_REPLENISHMENT_SESSION_URL + "?userId=" + userId
                        + "&currency=" + currency)
                .build()
                .delete()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    public Mono<CryptoReplenishmentSession> findCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                                                               CryptoReplenishmentSessionCurrency currency){
        return webClient.baseUrl(FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL + "?userId=" + userId
                        + "&currency=" + currency)
                .build()
                .get()
                .retrieve()
                .bodyToMono(CryptoReplenishmentSession.class);
    }
}
