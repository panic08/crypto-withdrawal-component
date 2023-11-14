package com.casino.cryptoreplenishmentprocess.api;

import com.casino.cryptoreplenishmentprocess.dto.tron.TrxTransactionsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TronGridApi {
    private static final String TRON_GRID_URL = "https://api.trongrid.io";

    public Mono<TrxTransactionsDto> getTrxAccountTransactions(String address){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(TRON_GRID_URL + "/v1/accounts/" + address + "/transactions?limit=4")
                .build()
                .get()
                .retrieve()
                .bodyToMono(TrxTransactionsDto.class);
    }
}
