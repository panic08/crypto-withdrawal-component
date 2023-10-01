package com.casino.replenishments.service.implement;

import com.casino.replenishments.dto.CryptoDataDto;
import com.casino.replenishments.dto.CryptoReplenishmentMessage;
import com.casino.replenishments.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishments.exception.CryptoReplenishmentExistsException;
import com.casino.replenishments.exception.IncorrectTokenProvidedException;
import com.casino.replenishments.mapper.CryptoReplenishmentMessageToCryptoReplenishmentResponseMapperImpl;
import com.casino.replenishments.mapper.CryptoReplenishmentRequestToCryptoReplenishmentMessageMapperImpl;
import com.casino.replenishments.model.CryptoReplenishmentSession;
import com.casino.replenishments.model.User;
import com.casino.replenishments.payload.children.CryptoReplenishmentBtcRequest;
import com.casino.replenishments.payload.children.CryptoReplenishmentEthRequest;
import com.casino.replenishments.payload.CryptoReplenishmentResponse;
import com.casino.replenishments.payload.children.CryptoReplenishmentTrxRequest;
import com.casino.replenishments.payload.children.CryptoReplenishmentUsdtRequest;
import com.casino.replenishments.property.ServicesIpProperty;
import com.casino.replenishments.service.CryptoReplenishmentService;
import com.casino.replenishments.util.NumberFormatterUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CryptoReplenishmentServiceImpl implements CryptoReplenishmentService {

    private final WebClient.Builder webClient;
    private final ServicesIpProperty servicesIpProperty;
    private final KafkaTemplate<String, CryptoReplenishmentMessage> kafkaTemplate;
    private final CryptoReplenishmentRequestToCryptoReplenishmentMessageMapperImpl cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper;
    private final CryptoReplenishmentMessageToCryptoReplenishmentResponseMapperImpl cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper;
    private static String FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static String DELETE_CRYPTO_REPLENISHMENT_SESSION_URL;
    private static final String CRYPTO_REPLENISHMENT_TOPIC = "crypto-replenishment-topic";
    private static String GET_INFO_BY_TOKEN_URL;
    private static String EXISTS_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static String GET_TRX_GENERATED_CRYPTO_DATA_URL;
    private static String GET_ETH_GENERATED_CRYPTO_DATA_URL;
    private static String GET_BTC_GENERATED_CRYPTO_DATA_URL;
    private static String SAVE_CRYPTO_REPLENISHMENT_SESSION_URL;
    @PostConstruct
    public void init() {
        FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/findCryptoReplenishmentSessionByUserIdAndCurrency";
        DELETE_CRYPTO_REPLENISHMENT_SESSION_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/deleteByUserIdAndCurrency";
        GET_INFO_BY_TOKEN_URL = "http://"
                + servicesIpProperty.getAuthIp()
                + ":8080/api/auth/getInfoByToken";
        EXISTS_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/existsCryptoReplenishmentSessionByUserIdAndCurrency";
        GET_TRX_GENERATED_CRYPTO_DATA_URL = "http://"
                + servicesIpProperty.getCryptoFactoryIp()
                + ":8082/api/crypto/trx/generate_data";
        GET_BTC_GENERATED_CRYPTO_DATA_URL = "http://"
                + servicesIpProperty.getCryptoFactoryIp()
                + ":8082/api/crypto/btc/generate_data";
        GET_ETH_GENERATED_CRYPTO_DATA_URL = "http://"
                + servicesIpProperty.getCryptoFactoryIp()
                + ":8082/api/crypto/eth/generate_data";
        SAVE_CRYPTO_REPLENISHMENT_SESSION_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/save";
    }

    @Override
    public Mono<CryptoReplenishmentSession> getCryptoReplenishmentSession(String authorization, CryptoReplenishmentSessionCurrency currency) {
        return getInfoByToken(authorization.split(" ")[1])
                .onErrorResume(err -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")))
                .flatMap(user -> findCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), currency));
    }

    @Override
    public Mono<Void> deleteCryptoReplenishmentSession(String authorization, CryptoReplenishmentSessionCurrency currency) {
        return getInfoByToken(authorization.split(" ")[1])
                .onErrorResume(err -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")))
                .flatMap(user -> deleteCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), currency));
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createTrxCryptoReplenishment(String authorization, CryptoReplenishmentTrxRequest cryptoReplenishmentTrxRequest) {
        Mono<User> userMono = getInfoByToken(authorization.split(" ")[1])
                .onErrorResume(err -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")));

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.TRX))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, generateCryptoData(GET_TRX_GENERATED_CRYPTO_DATA_URL))
                        .flatMap(tuple -> {
                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(tuple.getT2().getAddress());
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

                                        cryptoReplenishmentMessage.setRecipientAddress(tuple.getT2().getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(tuple.getT2().getPrivateKey());
                                        cryptoReplenishmentMessage.setRecipientPublicKey(tuple.getT2().getPublicKey());
                                        cryptoReplenishmentMessage.setId(cryptoReplenishmentSession1.getId());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createEthCryptoReplenishment(String authorization, CryptoReplenishmentEthRequest cryptoReplenishmentEthRequest) {
        Mono<User> userMono = getInfoByToken(authorization.split(" ")[1])
                .onErrorResume(err -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")));

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.ETH))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, generateCryptoData(GET_ETH_GENERATED_CRYPTO_DATA_URL))
                        .flatMap(tuple -> {
                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(tuple.getT2().getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentEthRequest.getAmount(), 4));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.ETH);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return saveCryptoReplenishmentSession(cryptoReplenishmentSession)
                                    .map(cryptoReplenishmentSession1 -> {
                                        CryptoReplenishmentMessage cryptoReplenishmentMessage =
                                                cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper
                                                        .cryptoReplenishmentRequestToCryptoReplenishmentMessage(cryptoReplenishmentEthRequest);

                                        cryptoReplenishmentMessage.setAmount(cryptoReplenishmentSession.getAmount());
                                        cryptoReplenishmentMessage.setCurrency(CryptoReplenishmentSessionCurrency.ETH);

                                        cryptoReplenishmentMessage.setRecipientAddress(tuple.getT2().getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(tuple.getT2().getPrivateKey());
                                        cryptoReplenishmentMessage.setRecipientPublicKey(tuple.getT2().getPublicKey());
                                        cryptoReplenishmentMessage.setId(cryptoReplenishmentSession1.getId());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createBtcCryptoReplenishment(String authorization, CryptoReplenishmentBtcRequest cryptoReplenishmentBtcRequest) {
        Mono<User> userMono = getInfoByToken(authorization.split(" ")[1])
                .onErrorResume(err -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")));

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.BTC))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, generateCryptoData(GET_BTC_GENERATED_CRYPTO_DATA_URL))
                        .flatMap(tuple -> {
                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(tuple.getT2().getAddress());
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

                                        cryptoReplenishmentMessage.setRecipientAddress(tuple.getT2().getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(tuple.getT2().getPrivateKey());
                                        cryptoReplenishmentMessage.setRecipientPublicKey(tuple.getT2().getPublicKey());
                                        cryptoReplenishmentMessage.setId(cryptoReplenishmentSession1.getId());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createUsdtTrc20CryptoReplenishment(String authorization, CryptoReplenishmentUsdtRequest cryptoReplenishmentUsdtRequest) {
        Mono<User> userMono = getInfoByToken(authorization.split(" ")[1])
                .onErrorResume(err -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")));

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.USDT_TRC20))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, generateCryptoData(GET_TRX_GENERATED_CRYPTO_DATA_URL))
                        .flatMap(tuple -> {
                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(tuple.getT2().getAddress());
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

                                        cryptoReplenishmentMessage.setRecipientAddress(tuple.getT2().getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(tuple.getT2().getPrivateKey());
                                        cryptoReplenishmentMessage.setRecipientPublicKey(tuple.getT2().getPublicKey());
                                        cryptoReplenishmentMessage.setId(cryptoReplenishmentSession1.getId());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    @Override
    public Mono<CryptoReplenishmentResponse> createUsdtErc20CryptoReplenishment(String authorization, CryptoReplenishmentUsdtRequest cryptoReplenishmentUsdtRequest) {
        Mono<User> userMono = getInfoByToken(authorization.split(" ")[1])
                .onErrorResume(err -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")));

        return userMono
                .flatMap(user -> existsCryptoReplenishmentSessionByUserIdAndCurrency(user.getId(), CryptoReplenishmentSessionCurrency.USDT_ERC20))
                .filter(aBoolean -> !aBoolean)
                .switchIfEmpty(Mono.error(new CryptoReplenishmentExistsException("Complete previous crypto-replenishment")))
                .flatMap(cryptoReplenishmentSession22 -> Mono.zip(userMono, generateCryptoData(GET_ETH_GENERATED_CRYPTO_DATA_URL))
                        .flatMap(tuple -> {
                            CryptoReplenishmentSession cryptoReplenishmentSession = new CryptoReplenishmentSession();

                            cryptoReplenishmentSession.setUserId(tuple.getT1().getId());
                            cryptoReplenishmentSession.setRecipientAddress(tuple.getT2().getAddress());
                            cryptoReplenishmentSession.setAmount(NumberFormatterUtil.formatDouble(cryptoReplenishmentUsdtRequest.getAmount(), 2));
                            cryptoReplenishmentSession.setCurrency(CryptoReplenishmentSessionCurrency.USDT_ERC20);
                            cryptoReplenishmentSession.setUntilTimestamp(System.currentTimeMillis() + (15 * 60 * 1000));

                            return saveCryptoReplenishmentSession(cryptoReplenishmentSession)
                                    .map(cryptoReplenishmentSession1 -> {
                                        CryptoReplenishmentMessage cryptoReplenishmentMessage =
                                                cryptoReplenishmentRequestToCryptoReplenishmentMessageMapper
                                                        .cryptoReplenishmentRequestToCryptoReplenishmentMessage(cryptoReplenishmentUsdtRequest);

                                        cryptoReplenishmentMessage.setAmount(cryptoReplenishmentSession.getAmount());
                                        cryptoReplenishmentMessage.setCurrency(CryptoReplenishmentSessionCurrency.USDT_ERC20);

                                        cryptoReplenishmentMessage.setRecipientAddress(tuple.getT2().getAddress());
                                        cryptoReplenishmentMessage.setRecipientPrivateKey(tuple.getT2().getPrivateKey());
                                        cryptoReplenishmentMessage.setRecipientPublicKey(tuple.getT2().getPublicKey());
                                        cryptoReplenishmentMessage.setId(cryptoReplenishmentSession1.getId());
                                        cryptoReplenishmentMessage.setUserId(tuple.getT1().getId());
                                        cryptoReplenishmentMessage.setUntilTimestamp(cryptoReplenishmentSession1.getUntilTimestamp());

                                        kafkaTemplate.send(CRYPTO_REPLENISHMENT_TOPIC, cryptoReplenishmentMessage);

                                        return cryptoReplenishmentMessageToCryptoReplenishmentResponseMapper
                                                .cryptoReplenishmentMessageToCryptoReplenishmentResponse(cryptoReplenishmentMessage);
                                    });
                        }));
    }

    private Mono<User> getInfoByToken(String token){
        return webClient.baseUrl(GET_INFO_BY_TOKEN_URL + "?token=" + token)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
    }

    private Mono<CryptoDataDto> generateCryptoData(String url){
        return webClient.baseUrl(url)
                .build()
                .get()
                .retrieve()
                .bodyToMono(CryptoDataDto.class);
    }
    private Mono<Void> deleteCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                                           CryptoReplenishmentSessionCurrency currency){
        return webClient.baseUrl(DELETE_CRYPTO_REPLENISHMENT_SESSION_URL + "?userId=" + userId
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
