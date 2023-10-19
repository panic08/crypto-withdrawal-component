package com.casino.cryptoreplenishmentprocess.listener;

import com.casino.cryptoreplenishmentprocess.dto.CryptoReplenishmentMessage;
import com.casino.cryptoreplenishmentprocess.service.implement.CryptoReplenishmentServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CryptoReplenishmentListener {
    private static final String CRYPTO_REPLENISHMENT_TOPIC = "crypto-replenishment-topic";
    private final CryptoReplenishmentServiceImpl cryptoReplenishmentService;

    @KafkaListener(topics = CRYPTO_REPLENISHMENT_TOPIC)
    public void handleCryptoReplenishment(String message){
            ObjectMapper objectMapper = new ObjectMapper();

            CryptoReplenishmentMessage cryptoReplenishmentMessage = null;
            try {
                cryptoReplenishmentMessage = objectMapper.readValue(message, CryptoReplenishmentMessage.class);
            } catch (Exception ignored){
            }

            switch (Objects.requireNonNull(cryptoReplenishmentMessage).getCurrency()){
                case BTC -> cryptoReplenishmentService.handleBtcCryptoReplenishment(cryptoReplenishmentMessage)
                        .subscribe();
                case ETH -> cryptoReplenishmentService.handleEthCryptoReplenishment(cryptoReplenishmentMessage)
                        .subscribe();
                case TRX -> cryptoReplenishmentService.handleTrxCryptoReplenishment(cryptoReplenishmentMessage)
                        .subscribe();
                case USDT_TRC20 -> cryptoReplenishmentService.handleUsdtTrc20CryptoReplenishment(cryptoReplenishmentMessage)
                        .subscribe();
                case BSC -> cryptoReplenishmentService.handleBscCryptoReplenishment(cryptoReplenishmentMessage)
                        .subscribe();
            }
    }
}
