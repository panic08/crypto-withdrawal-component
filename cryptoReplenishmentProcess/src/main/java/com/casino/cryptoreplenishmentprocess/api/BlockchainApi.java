package com.casino.cryptoreplenishmentprocess.api;

import com.casino.cryptoreplenishmentprocess.dto.bitcoin.BtcTransactionsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BlockchainApi {
    private static final String BLOCKCHAIN_URL = "https://blockchain.info";

    public Mono<BtcTransactionsDto> getBtcAccountTransactions(String address){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(BLOCKCHAIN_URL + "/rawaddr/" + address
                        + "?limit=4")
                .build()
                .get()
                .retrieve()
                .bodyToMono(BtcTransactionsDto.class);
    }
}
