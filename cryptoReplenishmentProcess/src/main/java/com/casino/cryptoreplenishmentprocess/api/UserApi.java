package com.casino.cryptoreplenishmentprocess.api;

import com.casino.cryptoreplenishmentprocess.model.UserData;
import com.casino.cryptoreplenishmentprocess.property.ServicesIpProperty;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserApi {
    private static String FIND_USERDATA_BY_USERID_URL;
    private static String UPDATE_USERDATA_BALANCE_BY_USERID_URL;
    private final ServicesIpProperty servicesIpProperty;
    private final WebClient.Builder webClient;

    @PostConstruct
    public void init() {
        FIND_USERDATA_BY_USERID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/findUserDataByUserId";
        UPDATE_USERDATA_BALANCE_BY_USERID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/updateBalanceByUserId";
    }

    public Mono<UserData> findUserDataByUserId(long userId){
        return webClient
                .baseUrl(FIND_USERDATA_BY_USERID_URL + "?userId=" + userId)
                .build()
                .get()
                .retrieve()
                .bodyToMono(UserData.class);
    }

    public Mono<Void> updateUserDataBalanceByUserId(long balance, long userId){
        return webClient
                .baseUrl(UPDATE_USERDATA_BALANCE_BY_USERID_URL + "?userId=" + userId
                        +  "&balance=" + balance)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

}
