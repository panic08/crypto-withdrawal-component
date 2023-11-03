package com.casino.replenishments.service.implement;

import com.casino.replenishments.api.ReplenishmentApi;
import com.casino.replenishments.api.UserApi;
import com.casino.replenishments.dto.CryptoReplenishmentMessage;
import com.casino.replenishments.dto.CryptoReplenishmentSessionDto;
import com.casino.replenishments.enums.CryptoDataCurrency;
import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishments.exception.CryptoReplenishmentExistsException;
import com.casino.replenishments.mapper.CryptoReplenishmentMessageToCryptoReplenishmentSessionDtoMapperImpl;
import com.casino.replenishments.mapper.CryptoReplenishmentRequestToCryptoReplenishmentMessageMapperImpl;
import com.casino.replenishments.mapper.CryptoReplenishmentSessionToCryptoReplenishmentSessionDtoMapperImpl;
import com.casino.replenishments.model.CryptoData;
import com.casino.replenishments.model.CryptoReplenishmentSession;
import com.casino.replenishments.model.User;
import com.casino.replenishments.payload.children.*;
import com.casino.replenishments.service.CryptoReplenishmentService;
import com.casino.replenishments.util.NumberFormatterUtil;
import com.casino.replenishments.util.RandomNumberGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CryptoReplenishmentServiceImpl implements CryptoReplenishmentService {

    private final KafkaTemplate<String, CryptoReplenishmentMessage> kafkaTemplate;
    private final CryptoReplenishmentRequestToCryptoReplenishmentMessageMapperImpl cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper;
    private final CryptoReplenishmentMessageToCryptoReplenishmentSessionDtoMapperImpl cryptoReplenishmentMessageToCryptoReplenishmentSessionDtoMapper;
    private final CryptoReplenishmentSessionToCryptoReplenishmentSessionDtoMapperImpl cryptoReplenishmentSessionToCryptoReplenishmentSessionDtoMapper;
    private static final String CRYPTO_REPLENISHMENT_TOPIC = "crypto-replenishment-topic";
    private final UserApi userApi;
    private final ReplenishmentApi replenishmentApi;


    @Override
    public Mono<CryptoReplenishmentSessionDto> getCryptoReplenishmentSession(long principalId, CryptoReplenishmentSessionCurrency currency) {
        return replenishmentApi.findCryptoReplenishmentSessionByUserIdAndCurrency(principalId, currency)
                .flatMap(cryptoReplenishmentSession -> Mono.just(cryptoReplenishmentSessionToCryptoReplenishmentSessionDtoMapper
                        .cryptoReplenishmentSessionToCryptoReplenishmentSessionDto(cryptoReplenishmentSession)));
    }

    @Override
    public Mono<Void> deleteCryptoReplenishmentSession(long principalId, CryptoReplenishmentSessionCurrency currency) {
        return replenishmentApi.deleteCryptoReplenishmentSessionByUserIdAndCurrency(principalId, currency);
    }

    @Override
    public Mono<CryptoReplenishmentSessionDto> createTrxCryptoReplenishment(long principalId, CryptoReplenishmentTrxRequest cryptoReplenishmentTrxRequest) {
        Mono<User> userMono = userApi.findUserById(principalId);

        return userMono
                .flatMap(user -> replenishmentApi.existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.TRX))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, replenishmentApi.findAllCryptoDataByCurrency(CryptoDataCurrency.TRX).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentTrxRequest.getAmount(), 1));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.TRX);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return replenishmentApi.saveCryptoReplenishmentSession(cryptoReplenishmentSession)
                                    .map(cryptoReplenishmentSession1 -> {
                                        CryptoReplenishmentMessage cryptoReplenishmentMessage =
                                                cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper
                                                        .cryptoReplenishmentRequestToCryptoReplenishmentMessage(cryptoReplenishmentTrxRequest);

                                        cryptoReplenishmentMessage.setAmount(cryptoReplenishmentSession.getAmount());
                                        cryptoReplenishmentMessage.setCurrency(CryptoReplenishmentSessionCurrency.TRX);

                                        cryptoReplenishmentMessage.setRecipientAddress(luckyCryptoData.getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(luckyCryptoData.getPrivateKey());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentSessionDtoMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentSessionDto(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentSessionDto> createEthCryptoReplenishment(long principalId, CryptoReplenishmentEthRequest cryptoReplenishmentEthRequest) {
        Mono<User> userMono = userApi.findUserById(principalId);

        return userMono
                .flatMap(user -> replenishmentApi.existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.ETH))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, replenishmentApi.findAllCryptoDataByCurrency(CryptoDataCurrency.ETH).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentEthRequest.getAmount(), 5));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.ETH);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return replenishmentApi.saveCryptoReplenishmentSession(cryptoReplenishmentSession)
                                    .map(cryptoReplenishmentSession1 -> {
                                        CryptoReplenishmentMessage cryptoReplenishmentMessage =
                                                cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper
                                                        .cryptoReplenishmentRequestToCryptoReplenishmentMessage(cryptoReplenishmentEthRequest);

                                        cryptoReplenishmentMessage.setAmount(cryptoReplenishmentSession.getAmount());
                                        cryptoReplenishmentMessage.setCurrency(CryptoReplenishmentSessionCurrency.ETH);

                                        cryptoReplenishmentMessage.setRecipientAddress(luckyCryptoData.getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(luckyCryptoData.getAddress());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentSessionDtoMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentSessionDto(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentSessionDto> createBtcCryptoReplenishment(long principalId, CryptoReplenishmentBtcRequest cryptoReplenishmentBtcRequest) {
        Mono<User> userMono = userApi.findUserById(principalId);

        return userMono
                .flatMap(user -> replenishmentApi.existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.BTC))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, replenishmentApi.findAllCryptoDataByCurrency(CryptoDataCurrency.BTC).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentBtcRequest.getAmount(), 6));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.BTC);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return replenishmentApi.saveCryptoReplenishmentSession(cryptoReplenishmentSession)
                                    .map(cryptoReplenishmentSession1 -> {
                                        CryptoReplenishmentMessage cryptoReplenishmentMessage =
                                                cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper
                                                        .cryptoReplenishmentRequestToCryptoReplenishmentMessage(cryptoReplenishmentBtcRequest);

                                        cryptoReplenishmentMessage.setAmount(cryptoReplenishmentSession.getAmount());
                                        cryptoReplenishmentMessage.setCurrency(CryptoReplenishmentSessionCurrency.BTC);

                                        cryptoReplenishmentMessage.setRecipientAddress(luckyCryptoData.getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(luckyCryptoData.getAddress());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentSessionDtoMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentSessionDto(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentSessionDto> createUsdtTrc20CryptoReplenishment(long principalId, CryptoReplenishmentUsdtRequest cryptoReplenishmentUsdtRequest) {
        Mono<User> userMono = userApi.findUserById(principalId);

        return userMono
                .flatMap(user -> replenishmentApi.existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.USDT_TRC20))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, replenishmentApi.findAllCryptoDataByCurrency(CryptoDataCurrency.TRX).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentUsdtRequest.getAmount(), 2));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.USDT_TRC20);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return replenishmentApi.saveCryptoReplenishmentSession(cryptoReplenishmentSession)
                                    .map(cryptoReplenishmentSession1 -> {
                                        CryptoReplenishmentMessage cryptoReplenishmentMessage =
                                                cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper
                                                        .cryptoReplenishmentRequestToCryptoReplenishmentMessage(cryptoReplenishmentUsdtRequest);

                                        cryptoReplenishmentMessage.setAmount(cryptoReplenishmentSession.getAmount());
                                        cryptoReplenishmentMessage.setCurrency(CryptoReplenishmentSessionCurrency.USDT_TRC20);

                                        cryptoReplenishmentMessage.setRecipientAddress(luckyCryptoData.getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(luckyCryptoData.getPrivateKey());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentSessionDtoMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentSessionDto(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentSessionDto> createBscCryptoReplenishment(long principalId, CryptoReplenishmentBscRequest cryptoReplenishmentBscRequest) {
        Mono<User> userMono = userApi.findUserById(principalId);

        return userMono
                .flatMap(user -> replenishmentApi.existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.BSC))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, replenishmentApi.findAllCryptoDataByCurrency(CryptoDataCurrency.BSC).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentBscRequest.getAmount(), 4));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.BSC);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return replenishmentApi.saveCryptoReplenishmentSession(cryptoReplenishmentSession)
                                    .map(cryptoReplenishmentSession1 -> {
                                        CryptoReplenishmentMessage cryptoReplenishmentMessage =
                                                cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper
                                                        .cryptoReplenishmentRequestToCryptoReplenishmentMessage(cryptoReplenishmentBscRequest);

                                        cryptoReplenishmentMessage.setAmount(cryptoReplenishmentSession.getAmount());
                                        cryptoReplenishmentMessage.setCurrency(CryptoReplenishmentSessionCurrency.BSC);

                                        cryptoReplenishmentMessage.setRecipientAddress(luckyCryptoData.getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(luckyCryptoData.getPrivateKey());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentSessionDtoMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentSessionDto(cryptoReplenishmentMessage);
                                    });
                        }));
    }
}
