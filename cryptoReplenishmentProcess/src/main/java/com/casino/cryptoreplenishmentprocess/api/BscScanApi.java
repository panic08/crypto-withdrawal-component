package com.casino.cryptoreplenishmentprocess.api;

import com.casino.cryptoreplenishmentprocess.dto.binancecoin.BscTransactionsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BscScanApi {
    private static final String BSC_SCAN_URL = "https://api.bscscan.com";

    public Mono<BscTransactionsDto> getBscAccountTransactions(String apiKey, String address){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(BSC_SCAN_URL + "/api?module=account&address=" + address
                        + "&sort=desc&apikey=" + apiKey
                        + "&offset=4&page=1&action=txlist")
                .build()
                .get()
                .retrieve()
                .bodyToMono(BscTransactionsDto.class);
    }
}
