package com.casino.replenishments.api;

import com.casino.replenishments.enums.CryptoDataCurrency;
import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishments.model.CryptoData;
import com.casino.replenishments.model.CryptoReplenishmentSession;
import com.casino.replenishments.model.Replenishment;
import com.casino.replenishments.property.ServicesIpProperty;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReplenishmentApi {
    private static String FIND_ALL_REPLENISHMENT_BY_ID_AND_DESC_WITH_LIMIT_URL;
    private static String FIND_ALL_REPLENISHMENT_BY_DESC_WITH_LIMIT_URL;
    private static String FIND_ALL_CRYPTO_DATA_URL;
    private static String DELETE_CRYPTO_DATA_BY_ID_URL;
    private static String SAVE_CRYPTO_DATA_URL;
    private static String FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static String DELETE_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static String EXISTS_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static String FIND_ALL_CRYPTO_DATA_BY_CURRENCY_URL;
    private static String SAVE_CRYPTO_REPLENISHMENT_SESSION_URL;
    private final WebClient.Builder webClient;
    private final ServicesIpProperty servicesIpProperty;

    @PostConstruct
    public void init() {
        FIND_ALL_REPLENISHMENT_BY_ID_AND_DESC_WITH_LIMIT_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/replenishment/findAllReplenishmentByIdAndDescWithLimit";
        FIND_ALL_REPLENISHMENT_BY_DESC_WITH_LIMIT_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/replenishment/findAllReplenishmentByDescWithLimit";
        FIND_ALL_CRYPTO_DATA_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoData/findAllCryptoData";
        SAVE_CRYPTO_DATA_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoData/save";
        DELETE_CRYPTO_DATA_BY_ID_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoData/deleteById";
        FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/findCryptoReplenishmentSessionByUserIdAndCurrency";
        DELETE_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/deleteByUserIdAndCurrency";
        EXISTS_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/existsByUserIdAndCurrency";
        FIND_ALL_CRYPTO_DATA_BY_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoData/findAllCryptoDataByCurrency";
        SAVE_CRYPTO_REPLENISHMENT_SESSION_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/save";
    }
    public Flux<Replenishment> findAllReplenishmentByIdAndDescWithLimit(long userId, int startIndex, int endIndex){
        return webClient.baseUrl(FIND_ALL_REPLENISHMENT_BY_ID_AND_DESC_WITH_LIMIT_URL + "?userId=" + userId
                        + "&startIndex=" + startIndex + "&endIndex=" + endIndex)
                .build()
                .get()
                .retrieve()
                .bodyToFlux(Replenishment.class);
    }

    public Flux<Replenishment> findAllReplenishmentByDescWithLimit(int startIndex, int endIndex){
        return webClient.baseUrl(FIND_ALL_REPLENISHMENT_BY_DESC_WITH_LIMIT_URL
                        + "?startIndex=" + startIndex + "&endIndex=" + endIndex)
                .build()
                .get()
                .retrieve()
                .bodyToFlux(Replenishment.class);
    }
    public Mono<CryptoData> saveCryptoData(CryptoData cryptoData){
        return webClient
                .baseUrl(SAVE_CRYPTO_DATA_URL)
                .build()
                .post()
                .bodyValue(cryptoData)
                .retrieve()
                .bodyToMono(CryptoData.class)
                .cache();
    }
    public Mono<Void> deleteCryptoDataById(long id){
        return webClient
                .baseUrl(DELETE_CRYPTO_DATA_BY_ID_URL + "?id=" + id)
                .build()
                .delete()
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Flux<CryptoData> findAllCryptoData(){
        return webClient.baseUrl(FIND_ALL_CRYPTO_DATA_URL)
                .build()
                .get()
                .retrieve()
                .bodyToFlux(CryptoData.class);
    }
    public Flux<CryptoData> findAllCryptoDataByCurrency(CryptoDataCurrency currency){
        return webClient.baseUrl(FIND_ALL_CRYPTO_DATA_BY_CURRENCY_URL + "?currency=" + currency)
                .build()
                .get()
                .retrieve()
                .bodyToFlux(CryptoData.class);
    }
    public Mono<Void> deleteCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                                           CryptoReplenishmentSessionCurrency currency){
        return webClient.baseUrl(DELETE_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL + "?userId=" + userId
                        + "&currency=" + currency)
                .build()
                .delete()
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<CryptoReplenishmentSession> saveCryptoReplenishmentSession(CryptoReplenishmentSession cryptoReplenishmentSession){
        return webClient.baseUrl(SAVE_CRYPTO_REPLENISHMENT_SESSION_URL)
                .build()
                .post()
                .bodyValue(cryptoReplenishmentSession)
                .retrieve()
                .bodyToMono(CryptoReplenishmentSession.class)
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

    public Mono<Boolean> existsCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                                              CryptoReplenishmentSessionCurrency currency){
        return webClient.baseUrl(EXISTS_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL + "?userId=" + userId
                        + "&currency=" + currency)
                .build()
                .get()
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
