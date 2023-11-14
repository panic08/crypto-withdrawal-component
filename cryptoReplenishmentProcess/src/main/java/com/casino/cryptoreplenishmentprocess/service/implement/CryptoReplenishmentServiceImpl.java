package com.casino.cryptoreplenishmentprocess.service.implement;

import com.casino.cryptoreplenishmentprocess.api.*;
import com.casino.cryptoreplenishmentprocess.dto.CoinsDataDto;
import com.casino.cryptoreplenishmentprocess.dto.CryptoReplenishmentMessage;
import com.casino.cryptoreplenishmentprocess.mapper.CryptoReplenishmentMessageToReplenishmentMapperImpl;
import com.casino.cryptoreplenishmentprocess.property.CryptoProvidersApiKeysProperty;
import com.casino.cryptoreplenishmentprocess.service.CryptoReplenishmentService;
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

    private final CryptoProvidersApiKeysProperty cryptoProvidersApiKeysProperty;
    private final CryptoReplenishmentMessageToReplenishmentMapperImpl cryptoReplenishmentMessageToReplenishmentMapper;
    private final ReplenishmentApi replenishmentApi;
    private final UserApi userApi;
    private final EtherScanApi etherScanApi;
    private final TronGridApi tronGridApi;
    private final BscScanApi bscScanApi;
    private final BlockchainApi blockchainApi;
    private static final String GET_COINS_DATA_URL = "https://api.coingecko.com/api/v3/simple/price?ids=tron,bitcoin,tether,ethereum,binancecoin&vs_currencies=rub";

    @Override
    public Mono<Void> handleTrxCryptoReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage) {
            return Mono.defer(() -> {
                AtomicBoolean untilSignal = new AtomicBoolean(false);

                log.info("Received message in method: handleTrxCryptoReplenishment, class: {}", CryptoReplenishmentServiceImpl.class);

                return Flux.interval(Duration.ofSeconds(4))
                        .take(Duration.ofMinutes(15))
                        .takeUntil(aBoolean -> untilSignal.get() || System.currentTimeMillis() >= cryptoReplenishmentMessage.getUntilTimestamp())
                        .flatMap(ignored -> Mono.zip(tronGridApi.getTrxAccountTransactions(cryptoReplenishmentMessage.getRecipientAddress()),
                                        replenishmentApi.findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
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
                                                                .flatMap(coinsDataDto -> userApi.findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                                        .flatMap(userData -> {
                                                                            long updatedBalance = userData.getBalance();

                                                                            double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                                    * coinsDataDto.getTronData().getRub();

                                                                            updatedBalance += updateOn * 100;

                                                                            return Mono.when(userApi.updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                                    replenishmentApi.saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                            .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                                        }));


                                                        return updateBalanceAndSaveReplenishment
                                                                .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                                    }

                                                    return Mono.empty();
                                                }))))
                        .then(replenishmentApi.deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
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
                    .flatMap(ignored -> Mono.zip(tronGridApi.getTrxAccountTransactions(cryptoReplenishmentMessage.getRecipientAddress()),
                                    replenishmentApi.findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
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
                                                            .flatMap(coinsDataDto -> userApi.findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                                    .flatMap(userData -> {
                                                                        long updatedBalance = userData.getBalance();

                                                                        double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                                * coinsDataDto.getTetherData().getRub();

                                                                        updatedBalance += updateOn * 100;

                                                                        return Mono.when(userApi.updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                                replenishmentApi.saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                        .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                                    }));


                                                    return updateBalanceAndSaveReplenishment
                                                            .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                                }

                                                return Mono.empty();
                                            }))))
                    .then(replenishmentApi.deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
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
                    .flatMap(ignored -> Mono.zip(etherScanApi.getEthAccountTransactions(
                            cryptoProvidersApiKeysProperty.getEtherScan(),
                            cryptoReplenishmentMessage.getRecipientAddress()
                                    ),
                                    replenishmentApi.findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
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
                                                    .flatMap(coinsDataDto -> userApi.findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                            .flatMap(userData -> {
                                                                long updatedBalance = userData.getBalance();

                                                                double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                        * coinsDataDto.getEthereumData().getRub();

                                                                updatedBalance += updateOn * 100;

                                                                return Mono.when(userApi.updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                        replenishmentApi.saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                            }));


                                            return updateBalanceAndSaveReplenishment
                                                    .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                        }

                                        return Mono.empty();
                                    })))
                    .then(replenishmentApi.deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
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
                    .flatMap(ignored -> Mono.zip(bscScanApi.getBscAccountTransactions(
                            cryptoProvidersApiKeysProperty.getBscScan(),
                            cryptoReplenishmentMessage.getRecipientAddress()
                                    ),
                                    replenishmentApi.findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
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
                                                    .flatMap(coinsDataDto -> userApi.findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                            .flatMap(userData -> {
                                                                long updatedBalance = userData.getBalance();

                                                                double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                        * coinsDataDto.getBinanceCoinData().getRub();

                                                                updatedBalance += updateOn * 100;

                                                                return Mono.when(userApi.updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                        replenishmentApi.saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                            }));


                                            return updateBalanceAndSaveReplenishment
                                                    .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                        }

                                        return Mono.empty();
                                    })))
                    .then(replenishmentApi.deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
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
                    .flatMap(ignored -> Mono.zip(blockchainApi.getBtcAccountTransactions(cryptoReplenishmentMessage.getRecipientAddress()),
                                    replenishmentApi.findCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(), cryptoReplenishmentMessage.getCurrency())
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
                                                            .flatMap(coinsDataDto -> userApi.findUserDataByUserId(cryptoReplenishmentMessage.getUserId())
                                                                    .flatMap(userData -> {
                                                                        long updatedBalance = userData.getBalance();

                                                                        double updateOn = cryptoReplenishmentMessage.getAmount()
                                                                                * coinsDataDto.getBitcoinData().getRub();

                                                                        updatedBalance += updateOn * 100;

                                                                        return Mono.when(userApi.updateUserDataBalanceByUserId(updatedBalance, cryptoReplenishmentMessage.getUserId()),
                                                                                replenishmentApi.saveReplenishment(cryptoReplenishmentMessageToReplenishmentMapper
                                                                                        .cryptoReplenishmentMessageToReplenishment(cryptoReplenishmentMessage)));
                                                                    }));


                                                    return updateBalanceAndSaveReplenishment
                                                            .then(Mono.fromRunnable(() -> untilSignal.set(true)));
                                                }

                                                return Mono.empty();
                                            }))))
                    .then(replenishmentApi.deleteCryptoReplenishmentSessionByUserIdAndCurrency(cryptoReplenishmentMessage.getUserId(),
                            cryptoReplenishmentMessage.getCurrency()));
        });
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

}
