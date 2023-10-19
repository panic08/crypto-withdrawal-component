package com.casino.cryptoreplenishmentprocess.service.implement;

import com.casino.cryptoreplenishmentprocess.dto.CoinsDataDto;
import com.casino.cryptoreplenishmentprocess.dto.CryptoReplenishmentMessage;
import com.casino.cryptoreplenishmentprocess.dto.binancecoin.BscTransactionsDto;
import com.casino.cryptoreplenishmentprocess.dto.bitcoin.BtcTransactionsDto;
import com.casino.cryptoreplenishmentprocess.dto.ethereum.EthTransactionsDto;
import com.casino.cryptoreplenishmentprocess.dto.tron.TrxTransactionsDto;
import com.casino.cryptoreplenishmentprocess.enums.CryptoReplenishmentSessionCurrency;
import com.casino.cryptoreplenishmentprocess.mapper.CryptoReplenishmentMessageToReplenishmentMapperImpl;
import com.casino.cryptoreplenishmentprocess.model.CryptoReplenishmentSession;
import com.casino.cryptoreplenishmentprocess.model.Replenishment;
import com.casino.cryptoreplenishmentprocess.model.UserData;
import com.casino.cryptoreplenishmentprocess.property.CryptoProvidersApiKeysProperty;
import com.casino.cryptoreplenishmentprocess.property.ServicesIpProperty;
import com.casino.cryptoreplenishmentprocess.service.CryptoReplenishmentService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoReplenishmentServiceImpl implements CryptoReplenishmentService {

    private final ServicesIpProperty servicesIpProperty;
    private final CryptoProvidersApiKeysProperty cryptoProvidersApiKeysProperty;
    private final CryptoReplenishmentMessageToReplenishmentMapperImpl cryptoReplenishmentMessageToReplenishmentMapper;
    private static String FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL;
    private static String DELETE_CRYPTO_REPLENISHMENT_SESSION_URL;
    private static final String GET_COINS_DATA_URL = "https://api.coingecko.com/api/v3/simple/price?ids=tron,bitcoin,tether,ethereum,binancecoin&vs_currencies=rub";
    private static String FIND_USERDATA_BY_USERID_URL;
    private static String UPDATE_USERDATA_BALANCE_BY_USERID_URL;
    private static String SAVE_REPLENISHMENT_URL;

    @PostConstruct
    public void init() {
        FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/findCryptoReplenishmentSessionByUserIdAndCurrency";
        DELETE_CRYPTO_REPLENISHMENT_SESSION_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/cryptoReplenishmentSession/deleteByUserIdAndCurrency";
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

                log.info("Received message in method: handleTrxCryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class);

                return Flux.interval(Duration.ofSeconds(4))
                        .take(Duration.ofMinutes(15))
                        .takeUntil(aBoolean -> untilSignal.get() || System.currentTimeMillis() >= cryptoReplenishmentMessage.getUntilTimestamp())
                        .flatMap(ignored -> Mono.zip(getTrxAccountTransactions(cryptoReplenishmentMessage.getRecipientAddress()),
                                        findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
                                                .switchIfEmpty(Mono.fromRunnable(() -> untilSignal.set(true))))
                                .doOnNext(d -> log.info("Executing Mono.zip in method: handleTrxCryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class))
                                .flatMapMany(tuple -> Flux.fromArray(tuple.getT1().getData())
                                        .filter(data -> data.getRets()[0].getContractRet().equals("SUCCESS")
                                                && data.getRawData().getTimestamp() > cryptoReplenishmentMessage.getUntilTimestamp() - 15 * 60 * 1000)
                                        .flatMap(data -> Flux.fromArray(data.getRawData().getContracts())
                                                .filter(contract -> contract.getType().equals("TransferContract")
                                                        && contract.getParameter().getValue().getToAddress().equals(cryptoReplenishmentMessage.getRecipientAddress())
                                                        && contract.getParameter().getValue().getAmount()/1e6 == cryptoReplenishmentMessage.getAmount())
                                                .hasElements()
                                                .flatMap(aBoolean -> {
                                                    if (aBoolean){
                                                        Mono<Void> updateBalanceAndSaveReplenishment = getCoinsData()
                                                                .flatMap(coinsDataDto -> findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                                        .flatMap(userData -> {
                                                                            long updatedBalance = userData.getBalance();

                                                                            double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                                    * coinsDataDto.getTronData().getRub();

                                                                            updatedBalance += updateOn * 100;

                                                                            return Mono.when(updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                                    saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                            .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                                        }));


                                                        return updateBalanceAndSaveReplenishment
                                                                .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                                    }

                                                    return Mono.empty();
                                                }))))
                        .then(deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
                                cryptoReplenishmentMessage.getCurrency()));
            });
    }

    @Override
    public Mono<Void> handleUsdtTrc20CryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.defer(() -> {
            AtomicBoolean untilSignal = new AtomicBoolean(false);

            log.info("Received message in method: handleUsdtTrc20CryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class);

            return Flux.interval(Duration.ofSeconds(4))
                    .take(Duration.ofMinutes(15))
                    .takeUntil(aBoolean -> untilSignal.get() || System.currentTimeMillis() >= cryptoReplenishmentMessage.getUntilTimestamp())
                    .flatMap(ignored -> Mono.zip(getTrxAccountTransactions(cryptoReplenishmentMessage.getRecipientAddress()),
                            findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
                                    .switchIfEmpty(Mono.fromRunnable(() -> untilSignal.set(true))))
                            .doOnNext(d -> log.info("Executing Mono.zip in method: handleUsdtTrc20CryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class))
                            .flatMapMany(tuple -> Flux.fromArray(tuple.getT1().getData())
                                    .filter(data -> data.getRets()[0].getContractRet().equals("SUCCESS")
                                            && data.getRawData().getTimestamp() > cryptoReplenishmentMessage.getUntilTimestamp() - 15 * 60 * 1000)
                                    .flatMap(data -> Flux.fromArray(data.getRawData().getContracts())
                                            .filter(contract -> contract.getType().equals("TriggerSmartContract")
                                                    && contract.getParameter().getValue().getToAddress().equals(cryptoReplenishmentMessage.getRecipientAddress())
                                                    && contract.getParameter().getValue().getContractAddress().equals("41a614f803b6fd780986a42c78ec9c7f77e6ded13c"))
                                            .filter(contract -> {
                                                BigInteger bigInteger = new BigInteger(
                                                        contract.getParameter().getValue().getData().substring(72),
                                                        16
                                                );

                                                return bigInteger.doubleValue()/1e6 == cryptoReplenishmentMessage.getAmount();
                                            })
                                            .hasElements()
                                            .flatMap(aBoolean -> {
                                                if (aBoolean){
                                                    Mono<Void> updateBalanceAndSaveReplenishment = getCoinsData()
                                                            .flatMap(coinsDataDto -> findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                                    .flatMap(userData -> {
                                                                        long updatedBalance = userData.getBalance();

                                                                        double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                                * coinsDataDto.getTetherData().getRub();

                                                                        updatedBalance += updateOn * 100;

                                                                        return Mono.when(updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                                saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                        .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                                    }));


                                                    return updateBalanceAndSaveReplenishment
                                                            .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                                }

                                                return Mono.empty();
                                            }))))
                    .then(deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
                            cryptoReplenishmentMessage.getCurrency()));

        });
    }


    @Override
    public Mono<Void> handleEthCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.defer(() -> {
            AtomicBoolean untilSignal = new AtomicBoolean(false);

            log.info("Received message in method: handleEthCryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class);

            return Flux.interval(Duration.ofSeconds(4))
                    .take(Duration.ofMinutes(15))
                    .takeUntil(aBoolean -> untilSignal.get() || System.currentTimeMillis() >= cryptoReplenishmentMessage.getUntilTimestamp())
                    .flatMap(ignored -> Mono.zip(getEthAccountTransactions(cryptoReplenishmentMessage.getRecipientAddress()),
                            findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
                                    .switchIfEmpty(Mono.fromRunnable(() -> untilSignal.set(true))))
                            .doOnNext(d -> log.info("Executing Mono.zip in method: handleEthCryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class))
                            .flatMapMany(tuple -> Flux.fromArray(tuple.getT1().getResults())
                                    .filter(result -> result.getTo().equals(cryptoReplenishmentMessage.getRecipientAddress())
                                            && Long.parseLong(result.getTimestamp()) > cryptoReplenishmentMessage.getUntilTimestamp() - 15 * 60 * 1000
                                            && Double.parseDouble(result.getValue())/1e18 == cryptoReplenishmentMessage.getAmount())
                                    .hasElements()
                                    .flatMap(aBoolean -> {
                                        if (aBoolean){
                                            Mono<Void> updateBalanceAndSaveReplenishment = getCoinsData()
                                                    .flatMap(coinsDataDto -> findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                            .flatMap(userData -> {
                                                                long updatedBalance = userData.getBalance();

                                                                double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                        * coinsDataDto.getEthereumData().getRub();

                                                                updatedBalance += updateOn * 100;

                                                                return Mono.when(updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                        saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                            }));


                                            return updateBalanceAndSaveReplenishment
                                                    .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                        }

                                        return Mono.empty();
                                    })))
                    .then(deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
                            cryptoReplenishmentMessage.getCurrency()));
        });
    }

    @Override
    public Mono<Void> handleBscCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.defer(() -> {
            AtomicBoolean untilSignal = new AtomicBoolean(false);

            log.info("Received message in method: handleBscCryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class);

            return Flux.interval(Duration.ofSeconds(4))
                    .take(Duration.ofMinutes(15))
                    .takeUntil(aBoolean -> untilSignal.get() || System.currentTimeMillis() >= cryptoReplenishmentMessage.getUntilTimestamp())
                    .flatMap(ignored -> Mono.zip(getBscAccountTransactions(cryptoReplenishmentMessage.getRecipientAddress()),
                                    findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
                                            .switchIfEmpty(Mono.fromRunnable(() -> untilSignal.set(true))))
                            .doOnNext(d -> log.info("Executing Mono.zip in method: handleBscCryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class))
                            .flatMapMany(tuple -> Flux.fromArray(tuple.getT1().getResults())
                                    .filter(result -> result.getTo().equals(cryptoReplenishmentMessage.getRecipientAddress())
                                            && Long.parseLong(result.getTimestamp()) > cryptoReplenishmentMessage.getUntilTimestamp() - 15 * 60 * 1000
                                            && Double.parseDouble(result.getValue())/1e18 == cryptoReplenishmentMessage.getAmount())
                                    .hasElements()
                                    .flatMap(aBoolean -> {
                                        if (aBoolean){
                                            Mono<Void> updateBalanceAndSaveReplenishment = getCoinsData()
                                                    .flatMap(coinsDataDto -> findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                            .flatMap(userData -> {
                                                                long updatedBalance = userData.getBalance();

                                                                double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                        * coinsDataDto.getBinanceCoinData().getRub();

                                                                updatedBalance += updateOn * 100;

                                                                return Mono.when(updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                        saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                            }));


                                            return updateBalanceAndSaveReplenishment
                                                    .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                        }

                                        return Mono.empty();
                                    })))
                    .then(deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
                            cryptoReplenishmentMessage.getCurrency()));
        });
    }

    @Override
    public Mono<Void> handleBtcCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
        return Mono.defer(() -> {
            AtomicBoolean untilSignal = new AtomicBoolean(false);

            log.info("Received message in method: handleBtcCryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class);

            return Flux.interval(Duration.ofSeconds(4))
                    .take(Duration.ofMinutes(15))
                    .takeUntil(aBoolean -> untilSignal.get() || System.currentTimeMillis() >= cryptoReplenishmentMessage.getUntilTimestamp())
                    .flatMap(ignored -> Mono.zip(getBtcAccountTransactions(cryptoReplenishmentMessage.getRecipientAddress()),
                                    findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
                                            .switchIfEmpty(Mono.fromRunnable(() -> untilSignal.set(true))))
                            .doOnNext(d -> log.info("Executing Mono.zip in method: handleBtcCryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class))
                            .flatMapMany(tuple -> Flux.fromArray(tuple.getT1().getTxs())
                                    .filter(tx -> tx.getTime() > cryptoReplenishmentMessage.getUntilTimestamp() - 15 * 60 * 1000)
                                    .flatMap(tx -> Flux.fromArray(tx.getOuts())
                                            .filter(out -> out.getAddr().equals(cryptoReplenishmentMessage.getRecipientAddress())
                                            && out.getValue()/1e8 == cryptoReplenishmentMessage.getAmount())
                                            .hasElements()
                                            .flatMap(aBoolean -> {
                                                if (aBoolean){
                                                    Mono<Void> updateBalanceAndSaveReplenishment = getCoinsData()
                                                            .flatMap(coinsDataDto -> findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                                    .flatMap(userData -> {
                                                                        long updatedBalance = userData.getBalance();

                                                                        double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                                * coinsDataDto.getBitcoinData().getRub();

                                                                        updatedBalance += updateOn * 100;

                                                                        return Mono.when(updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                                saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                        .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                                    }));


                                                    return updateBalanceAndSaveReplenishment
                                                            .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                                }

                                                return Mono.empty();
                                            }))))
                    .then(deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
                            cryptoReplenishmentMessage.getCurrency()));
        });
    }

    private Mono<TrxTransactionsDto> getTrxAccountTransactions(String address){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl("https://api.trongrid.io/v1/accounts/" + address + "/transactions?limit=4")
                .build()
                .get()
                .retrieve()
                .bodyToMono(TrxTransactionsDto.class);
    }

    private Mono<EthTransactionsDto> getEthAccountTransactions(String address){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl("https://api.etherscan.io/api?module=account&address=" + address
                + "&sort=desc&apikey=" + cryptoProvidersApiKeysProperty.getEtherScan()
                + "&offset=4&page=1&action=txlist")
                .build()
                .get()
                .retrieve()
                .bodyToMono(EthTransactionsDto.class);
    }

    private Mono<BscTransactionsDto> getBscAccountTransactions(String address){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl("https://api.bscscan.com/api?module=account&address=" + address
                        + "&sort=desc&apikey=" + cryptoProvidersApiKeysProperty.getBscScan()
                        + "&offset=4&page=1&action=txlist")
                .build()
                .get()
                .retrieve()
                .bodyToMono(BscTransactionsDto.class);
    }

    private Mono<BtcTransactionsDto> getBtcAccountTransactions(String address){
        WebClient.Builder webClient = WebClient.builder();

        return webClient
                .baseUrl("https://blockchain.info/rawaddr/" + address
                + "?limit=4")
                .build()
                .get()
                .retrieve()
                .bodyToMono(BtcTransactionsDto.class);
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

        return webClient.baseUrl(FIND_CRYPTO_REPLENISHMENT_SESSION_BY_USERID_AND_CURRENCY_URL + "?userId=" + userId
                + "&currency=" + currency)
                .build()
                .get()
                .retrieve()
                .bodyToMono(CryptoReplenishmentSession.class);
    }
}
