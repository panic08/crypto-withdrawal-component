package com.casino.replenishments.service.implement;

import com.casino.replenishments.enums.UserRole;
import com.casino.replenishments.exception.UnauthorizedRoleException;
import com.casino.replenishments.mapper.CryptoDataCreatePayloadToCryptoDataMapperImpl;
import com.casino.replenishments.model.CryptoData;
import com.casino.replenishments.model.User;
import com.casino.replenishments.payload.CryptoDataCreatePayload;
import com.casino.replenishments.property.ServicesIpProperty;
import com.casino.replenishments.service.CryptoDataService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CryptoDataServiceImpl implements CryptoDataService {
    private static String SAVE_CRYPTO_DATA_URL;
    private final ServicesIpProperty servicesIpProperty;
    private final WebClient.Builder webClient;
    private final CryptoDataCreatePayloadToCryptoDataMapperImpl cryptoDataCreatePayloadToCryptoDataMapper;
    private static String FIND_ORIGINAL_USER_BY_ID_URL;
    private static String FIND_ALL_CRYPTO_DATA_URL;
    private static String DELETE_CRYPTO_DATA_BY_ID_URL;

    @PostConstruct
    public void init() {
        FIND_ORIGINAL_USER_BY_ID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findOriginalUserById";
        SAVE_CRYPTO_DATA_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoData/save";
        DELETE_CRYPTO_DATA_BY_ID_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoData/deleteById";
        FIND_ALL_CRYPTO_DATA_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoData/findAllCryptoData";
    }

    @Override
    public Mono<CryptoDataCreatePayload> createCryptoData(long userId, CryptoDataCreatePayload cryptoDataCreatePayload) {
        return findOriginalUserById(userId)
                .flatMap(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        return saveCryptoData(cryptoDataCreatePayloadToCryptoDataMapper
                                .cryptoDataCreatePayloadToCryptoData(cryptoDataCreatePayload))
                                .thenReturn(cryptoDataCreatePayload);
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

    @Override
    public Flux<CryptoData> getAllCryptoData(long userId) {
        return findOriginalUserById(userId)
                .flatMapMany(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        return findAllCryptoData();
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

    @Override
    public Mono<Void> deleteCryptoData(long userId, long id) {
        return findOriginalUserById(userId)
                .flatMap(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        return deleteCryptoDataById(id);
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

    private Mono<User> findOriginalUserById(long id){
        return webClient
                .baseUrl(FIND_ORIGINAL_USER_BY_ID_URL + "?id=" + id)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }
    private Mono<CryptoData> saveCryptoData(CryptoData cryptoData){
        return webClient
                .baseUrl(SAVE_CRYPTO_DATA_URL)
                .build()
                .post()
                .bodyValue(cryptoData)
                .retrieve()
                .bodyToMono(CryptoData.class)
                .cache();
    }
    private Mono<Void> deleteCryptoDataById(long id){
        return webClient
                .baseUrl(DELETE_CRYPTO_DATA_BY_ID_URL + "?id=" + id)
                .build()
                .delete()
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Flux<CryptoData> findAllCryptoData(){
        return webClient.baseUrl(FIND_ALL_CRYPTO_DATA_URL)
                .build()
                .get()
                .retrieve()
                .bodyToFlux(CryptoData.class);
    }
}
