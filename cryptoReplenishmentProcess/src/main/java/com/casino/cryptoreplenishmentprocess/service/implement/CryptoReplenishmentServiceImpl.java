package com.casino.cryptoreplenishmentprocess.service.implement;

import com.casino.cryptoreplenishmentprocess.dto.CoinsDataDto;
import com.casino.cryptoreplenishmentprocess.dto.CryptoReplenishmentMessage;
import com.casino.cryptoreplenishmentprocess.dto.tron.TrxTransactionsDto;
import com.casino.cryptoreplenishmentprocess.enums.CryptoReplenishmentSessionCurrency;
import com.casino.cryptoreplenishmentprocess.mapper.CryptoReplenishmentMessageToReplenishmentMapperImpl;
import com.casino.cryptoreplenishmentprocess.model.CryptoReplenishmentSession;
import com.casino.cryptoreplenishmentprocess.model.Replenishment;
import com.casino.cryptoreplenishmentprocess.model.UserData;
import com.casino.cryptoreplenishmentprocess.property.ServicesIpProperty;
import com.casino.cryptoreplenishmentprocess.service.CryptoReplenishmentService;
import com.casino.cryptoreplenishmentprocess.util.NumberFormatterUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class CryptoReplenishmentServiceImpl implements CryptoReplenishmentService {

    private final ServicesIpProperty servicesIpProperty;
    private final CryptoReplenishmentMessageToReplenishmentMapperImpl cryptoReplenishmentMessageToReplenishmentMapper;
    private static String FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY;
    private static String DELETE_CRYPTO_REPLENISHMENT_SESSION_URL;
    private static final String GET_COINS_DATA_URL = "https://api.coingecko.com/api/v3/simple/price?ids=tron&vs_currencies=usd";
    private static String FIND_USERDATA_BY_USERID_URL;
    private static String UPDATE_USERDATA_BALANCE_BY_USERID_URL;
    private static String SAVE_REPLENISHMENT_URL;

    @PostConstruct
    public void init() {
        FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/findCryptoReplenishmentSessionByUserIdAndCurrency";
        DELETE_CRYPTO_REPLENISHMENT_SESSION_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/deleteCryptoReplenishmentSessionByUserIdAndCurrency";
        FIND_USERDATA_BY_USERID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/findUserDataByUserId";
        UPDATE_USERDATA_BALANCE_BY_USERID_URL = "http://"
                + servicesIpProperty.getUserApiIp()
                + ":8081/api/userData/updateBalanceByUserId";
        SAVE_REPLENISHMENT_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/replenishment/save";
    }

    @Override
    public Mono<Void> handleTrxCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
            return Mono.defer(() -> {
                AtomicBoolean untilSignal = new AtomicBoolean(false);

                return Flux.interval(Duration.ofSeconds(4))
                        .take(Duration.ofMinutes(15))
                        .takeUntil(aBoolean -> untilSignal.get() || System.currentTimeMillis() >= cryptoReplenishmentMessage.getUntilTimestamp())
                        .flatMap(ignored -> Mono.zip(getTrxAccountTransactions(cryptoReplenishmentMessage.getRecipientAddress()),
                                        findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), CryptoReplenishmentSessionCurrency.TRX)
                                                .switchIfEmpty(Mono.fromRunnable(() -> untilSignal.set(true))))
                                .flatMapMany(tuple -> Flux.fromArray(tuple.getT1().getData())
                                        .filter(data -> data.getRets()[0].getContractRet().equals("SUCCESS"))
                                        .filter(data -> data.getRawData().getTimestamp() > cryptoReplenishmentMessage.getUntilTimestamp() - 15 * 60 * 1000)
                                        .flatMap(data -> Flux.fromArray(data.getRawData().getContracts())
                                                .filter(contract -> contract.getType().equals("TransferContract"))
                                                .filter(contract -> contract.getParameter().getValue().getAmount()/1e6 == cryptoReplenishmentMessage.getAmount())
                                                .hasElements()
                                                .flatMap(aBoolean -> {
                                                    if (aBoolean){
                                                        Mono<Void> updateBalanceAndSaveReplenishment = getCoinsData()
                                                                .flatMap(coinsDataDto -> findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                                        .flatMap(userData -> {
                                                                            long updatedBalance = userData.getBalance();

                                                                            double updateOn = NumberFormatterUtil.formatDouble(cryptoReplenishmentMessage.getAmount()
                                                                                    * coinsDataDto.getTronData().getUsd(), 2);

                                                                            updatedBalance += updateOn * 100;

                                                                            return Mono.when(updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                                    saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                            .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                                        }));


                                                        return updateBalanceAndSaveReplenishment
                                                                .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                                    }

                                                    return Mono.empty();
                                                })))).then();
            }).then(deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
                    cryptoReplenishmentMessage.getCurrency()));
    }


    @Override
    public Mono<Void> handleEthCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleBtcCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleUsdtTrc20CryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleUsdtErc20CryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.empty();
    }

    private Mono<TrxTransactionsDto> getTrxAccountTransactions(String address){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl("https://api.trongrid.io/v1/accounts/" + address + "/transactions?limit=3")
                .build()
                .get()
                .retrieve()
                .bodyToMono(TrxTransactionsDto.class);
    }

    private Mono<CoinsDataDto> getCoinsData(){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(GET_COINS_DATA_URL)
                .build()
                .get()
                .retrieve()
                .bodyToMono(CoinsDataDto.class);
    }

    private Mono<UserData> findUserDataByUserId(long userId){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(FIND_USERDATA_BY_USERID_URL + "?userId=" + userId)
                .build()
                .get()
                .retrieve()
                .bodyToMono(UserData.class);
    }

    private Mono<Replenishment> saveReplenishment(Replenishment replenishment){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(SAVE_REPLENISHMENT_URL)
                .build()
                .post()
                .bodyValue(replenishment)
                .retrieve()
                .bodyToMono(Replenishment.class)
                .cache();
    }

    private Mono<Void> updateUserDataBalanceByUserId(long balance, long userId){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl(UPDATE_USERDATA_BALANCE_BY_USERID_URL + "?userId=" + userId
                +  "&balance=" + balance)
                .build()
                .put()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    private Mono<Void> deleteCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                                           CryptoReplenishmentSessionCurrency currency){
        WebClient.Builder webClient = WebClient.builder();

        return webClient.baseUrl(DELETE_CRYPTO_REPLENISHMENT_SESSION_URL + "?userId=" + userId
                        + "&currency=" + currency)
                .build()
                .delete()
                .retrieve()
                .bodyToMono(Void.class)
                .cache();
    }

    private Mono<CryptoReplenishmentSession> findCryptoReplenishmentSessionByUserIdAndCurrency(long userId,
                                                                                               CryptoReplenishmentSessionCurrency currency){
        WebClient.Builder webClient = WebClient.builder();

        return webClient.baseUrl(FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY + "?userId=" + userId
                + "&currency=" + currency)
                .build()
                .get()
                .retrieve()
                .bodyToMono(CryptoReplenishmentSession.class);
    }
}
