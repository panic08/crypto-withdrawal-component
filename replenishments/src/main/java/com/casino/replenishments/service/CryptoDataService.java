package com.casino.replenishments.service;

import com.casino.replenishments.dto.CryptoDataDto;
import com.casino.replenishments.model.CryptoData;
import com.casino.replenishments.payload.CryptoDataCreatePayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CryptoDataService {
    Mono<CryptoDataCreatePayload> createCryptoData(long principalId, CryptoDataCreatePayload cryptoDataCreatePayload);
    Flux<CryptoDataDto> getAllCryptoData(long principalId);
    Mono<Void> deleteCryptoData(long principalId, long id);
}
