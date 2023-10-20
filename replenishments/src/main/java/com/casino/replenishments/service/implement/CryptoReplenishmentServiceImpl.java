package com.casino.replenishments.service.implement;

import com.casino.replenishments.dto.CryptoReplenishmentMessage;
import com.casino.replenishments.dto.CryptoReplenishmentSessionDto;
import com.casino.replenishments.enums.CryptoDataCurrency;
import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishments.exception.CryptoReplenishmentExistsException;
import com.casino.replenishments.mapper.CryptoReplenishmentMessageToCryptoReplenishmentResponseMapperImpl;
import com.casino.replenishments.mapper.CryptoReplenishmentRequestToCryptoReplenishmentMessageMapperImpl;
import com.casino.replenishments.mapper.CryptoReplenishmentSessionToCryptoReplenishmentSessionDtoMapperImpl;
import com.casino.replenishments.model.CryptoData;
import com.casino.replenishments.model.CryptoReplenishmentSession;
import com.casino.replenishments.model.User;
import com.casino.replenishments.payload.children.*;
import com.casino.replenishments.payload.CryptoReplenishmentResponse;
import com.casino.replenishments.property.ServicesIpProperty;
import com.casino.replenishments.service.CryptoReplenishmentService;
import com.casino.replenishments.util.NumberFormatterUtil;
import com.casino.replenishments.util.RandomNumberGeneratorUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CryptoReplenishmentServiceImpl implements CryptoReplenishmentService {

    private final WebClient.Builder webClient;
    private final ServicesIpProperty servicesIpProperty;
    private final KafkaTemplate<String, CryptoReplenishmentMessage> kafkaTemplate;
    private final CryptoReplenishmentRequestToCryptoReplenishmentMessageMapperImpl cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper;
    private final CryptoReplenishmentMessageToCryptoReplenishmentResponseMapperImpl cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper;
    private final CryptoReplenishmentSessionToCryptoReplenishmentSessionDtoMapperImpl cryptoReplenishmentSessionToCryptoReplenishmentSessionDtoMapper;
    private static String FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static String DELETE_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static final String CRYPTO_REPLENISHMENT_TOPIC = "crypto-replenishment-topic";
    private static String FIND_USER_BY_ID_URL;
    private static String EXISTS_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static String FIND_ALL_CRYPTO_DATA_BY_CURRENCY_URL;
    private static String SAVE_CRYPTO_REPLENISHMENT_SESSION_URL;
    @PostConstruct
    public void init() {
        FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/findCryptoReplenishmentSessionByUserIdAndCurrency";
        DELETE_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/deleteByUserIdAndCurrency";
        FIND_USER_BY_ID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/user/findUserById";
        EXISTS_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/existsByUserIdAndCurrency";
        FIND_ALL_CRYPTO_DATA_BY_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoData/findAllCryptoDataByCurrency";
        SAVE_CRYPTO_REPLENISHMENT_SESSION_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/save";
    }

    @Override
    public Mono<CryptoReplenishmentSessionDto> getCryptoReplenishmentSession(long userId, CryptoReplenishmentSessionCurrency currency) {
        return findCryptoReplenishmentSessionByUserIdAndCurrency(userId, currency)
                .flatMap(cryptoReplenishmentSession -> Mono.just(cryptoReplenishmentSessionToCryptoReplenishmentSessionDtoMapper
                        .cryptoReplenishmentSessionToCryptoReplenishmentSessionDto(cryptoReplenishmentSession)));
    }

    @Override
    public Mono<Void> deleteCryptoReplenishmentSession(long userId, CryptoReplenishmentSessionCurrency currency) {
        return deleteCryptoReplenishmentSessionByUserIdAndCurrency(userId, currency);
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createTrxCryptoReplenishment(long userId, CryptoReplenishmentTrxRequest cryptoReplenishmentTrxRequest) {
        Mono<User> userMono = findUserById(userId);

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.TRX))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, findAllCryptoDataByCurrency(CryptoDataCurrency.TRX).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentTrxRequest.getAmount(), 1));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.TRX);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return saveCryptoReplenishmentSession(cryptoReplenishmentSession)
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

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createEthCryptoReplenishment(long userId, CryptoReplenishmentEthRequest cryptoReplenishmentEthRequest) {
        Mono<User> userMono = findUserById(userId);

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.ETH))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, findAllCryptoDataByCurrency(CryptoDataCurrency.ETH).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentEthRequest.getAmount(), 5));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.ETH);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return saveCryptoReplenishmentSession(cryptoReplenishmentSession)
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

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createBtcCryptoReplenishment(long userId, CryptoReplenishmentBtcRequest cryptoReplenishmentBtcRequest) {
        Mono<User> userMono = findUserById(userId);

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.BTC))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, findAllCryptoDataByCurrency(CryptoDataCurrency.BTC).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentBtcRequest.getAmount(), 6));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.BTC);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return saveCryptoReplenishmentSession(cryptoReplenishmentSession)
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

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createUsdtTrc20CryptoReplenishment(long userId, CryptoReplenishmentUsdtRequest cryptoReplenishmentUsdtRequest) {
        Mono<User> userMono = findUserById(userId);

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.USDT_TRC20))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, findAllCryptoDataByCurrency(CryptoDataCurrency.TRX).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentUsdtRequest.getAmount(), 2));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.USDT_TRC20);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return saveCryptoReplenishmentSession(cryptoReplenishmentSession)
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

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createBscCryptoReplenishment(long userId, CryptoReplenishmentBscRequest cryptoReplenishmentBscRequest) {
        Mono<User> userMono = findUserById(userId);

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.BSC))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, findAllCryptoDataByCurrency(CryptoDataCurrency.BSC).collectList())
                        .flatMap(tuple -> {
                            CryptoData luckyCryptoData = tuple.getT2().get(RandomNumberGeneratorUtil.generateRandomNumber(0, tuple.getT2().size() - 1));

                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(luckyCryptoData.getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentBscRequest.getAmount(), 4));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.BSC);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return saveCryptoReplenishmentSession(cryptoReplenishmentSession)
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

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    private Mono<User> findUserById(long id){
        return webClient
                .baseUrl(FIND_USER_BY_ID_URL + "?id=" + id)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }

    private Flux<CryptoData> findAllCryptoDataByCurrency(CryptoDataCurrency currency){
        return webClient.baseUrl(FIND_ALL_CRYPTO_DATA_BY_CURRENCY_URL + "?currency=" + currency)
                .build()
                .get()
                .retrieve()
                .bodyToFlux(CryptoData.class);
    }
    private Mono<Void> deleteCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                                           CryptoReplenishmentSessionCurrency currency){
        return webClient.baseUrl(DELETE_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL + "?userId=" + userId
                + "&currency=" + currency)
                .build()
                .delete()
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Mono<CryptoReplenishmentSession> saveCryptoReplenishmentSession(CryptoReplenishmentSession cryptoReplenishmentSession){
        return webClient.baseUrl(SAVE_CRYPTO_REPLENISHMENT_SESSION_URL)
                .build()
                .post()
                .bodyValue(cryptoReplenishmentSession)
                .retrieve()
                .bodyToMono(CryptoReplenishmentSession.class)
                .cache();
    }

    private Mono<CryptoReplenishmentSession> findCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                                                               CryptoReplenishmentSessionCurrency currency){
        return webClient.baseUrl(FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL + "?userId=" + userId
        + "&currency=" + currency)
                .build()
                .get()
                .retrieve()
                .bodyToMono(CryptoReplenishmentSession.class);
    }

    private Mono<Boolean> existsCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                    CryptoReplenishmentSessionCurrency currency){
        return webClient.baseUrl(EXISTS_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL + "?userId=" + userId
        + "&currency=" + currency)
                .build()
                .get()
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
