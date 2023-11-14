package com.casino.cryptoreplenishmentprocess.api;

import com.casino.cryptoreplenishmentprocess.dto.ethereum.EthTransactionsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EtherScanApi {
    private static final String ETHER_SCAN_URL = "https://api.etherscan.io/api";

    public Mono<EthTransactionsDto> getEthAccountTransactions(String apiKey, String address){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(ETHER_SCAN_URL + "/api?module=account&address=" + address
                        + "&sort=desc&apikey=" + apiKey
                        + "&offset=4&page=1&action=txlist")
                .build()
                .get()
                .retrieve()
                .bodyToMono(EthTransactionsDto.class);
    }
}
