package com.casino.replenishments.service.implement;

import com.casino.replenishments.api.ReplenishmentApi;
import com.casino.replenishments.api.UserApi;
import com.casino.replenishments.dto.CryptoDataDto;
import com.casino.replenishments.enums.UserRole;
import com.casino.replenishments.exception.UnauthorizedRoleException;
import com.casino.replenishments.mapper.CryptoDataCreatePayloadToCryptoDataMapperImpl;
import com.casino.replenishments.mapper.CryptoDataToCryptoDataDtoMapperImpl;
import com.casino.replenishments.payload.CryptoDataCreatePayload;
import com.casino.replenishments.service.CryptoDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CryptoDataServiceImpl implements CryptoDataService {

    private final CryptoDataCreatePayloadToCryptoDataMapperImpl cryptoDataCreatePayloadToCryptoDataMapper;
    private final CryptoDataToCryptoDataDtoMapperImpl cryptoDataToCryptoDataDtoMapper;
    private final UserApi userApi;
    private final ReplenishmentApi replenishmentApi;

    @Override
    public Mono<CryptoDataCreatePayload> createCryptoData(long principalId, CryptoDataCreatePayload cryptoDataCreatePayload) {
        return userApi.findUserById(principalId)
                .flatMap(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        return replenishmentApi.saveCryptoData(cryptoDataCreatePayloadToCryptoDataMapper
                                .cryptoDataCreatePayloadToCryptoData(cryptoDataCreatePayload))
                                .thenReturn(cryptoDataCreatePayload);
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

    @Override
    public Flux<CryptoDataDto> getAllCryptoData(long principalId) {
        return userApi.findUserById(principalId)
                .flatMapMany(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        return replenishmentApi.findAllCryptoData().flatMap(cryptoData -> Mono.just(cryptoDataToCryptoDataDtoMapper.cryptoDataToCryptoDataDto(cryptoData)));
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

    @Override
    public Mono<Void> deleteCryptoData(long principalId, long id) {
        return userApi.findUserById(principalId)
                .flatMap(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        return replenishmentApi.deleteCryptoDataById(id);
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }
}
