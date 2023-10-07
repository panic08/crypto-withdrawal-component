package com.casino.replenishmentapi.api;

import com.casino.replenishmentapi.enums.CryptoDataCurrency;
import com.casino.replenishmentapi.enums.CryptoReplenishmentSessionCurrency;
import com.casino.replenishmentapi.model.CryptoData;
import com.casino.replenishmentapi.model.CryptoReplenishmentSession;
import com.casino.replenishmentapi.model.Replenishment;
import com.casino.replenishmentapi.repository.CryptoDataRepository;
import com.casino.replenishmentapi.repository.CryptoReplenishmentSessionRepository;
import com.casino.replenishmentapi.repository.ReplenishmentRepository;
import com.casino.replenishmentapi.repository.implement.CryptoReplenishmentSessionRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8084")
@RequiredArgsConstructor
public class ReplenishmentApi {

    private final ReplenishmentRepository replenishmentRepository;
    private final CryptoDataRepository cryptoDataRepository;
    private final CryptoReplenishmentSessionRepositoryImpl cryptoReplenishmentSessionRepository;

    @GetMapping("/replenishment/findAllOriginalReplenishmentByIdWithLimit")
    public Flux<Replenishment> findAllOriginalReplenishmentWithLimit(
            @RequestParam("userId") long userId,
            @RequestParam("startIndex") int startIndex,
            @RequestParam("endIndex") int endIndex
    ) {
        return replenishmentRepository.findAllByUserIdByCreatedAtDesc(userId, startIndex, endIndex);
    }

    @GetMapping("/cryptoReplenishmentSession/findCryptoReplenishmentSessionByUserIdAndCurrency")
    public Mono<CryptoReplenishmentSession> findCryptoReplenishmentSessionByUserIdAndCurrency(
            @RequestParam("userId") long userId,
            @RequestParam("currency") CryptoReplenishmentSessionCurrency currency
            ){
        return cryptoReplenishmentSessionRepository
                .findCryptoReplenishmentSessionByUserIdAndCurrency(userId, currency);
    }

    @GetMapping("/cryptoReplenishmentSession/existsCryptoReplenishmentSessionByUserIdAndCurrency")
    public Mono<Boolean> existsByUserIdAndCurrency(
            @RequestParam("userId") long userId,
            @RequestParam("currency") CryptoReplenishmentSessionCurrency currency
    ){
        return cryptoReplenishmentSessionRepository
                .findCryptoReplenishmentSessionByUserIdAndCurrency(userId, currency)
                .map(cryptoReplenishmentSession -> true)
                .defaultIfEmpty(false);
    }

    @GetMapping("/cryptoData/findAllCryptoDataByCurrency")
    public Flux<CryptoData> findAllCryptoDataByCurrency(@RequestParam("currency") CryptoDataCurrency currency){
        return cryptoDataRepository.findAllCryptoDataByCurrency(currency);
    }

    @PostMapping("/replenishment/save")
    public Mono<Replenishment> saveReplenishment(@RequestBody Replenishment replenishment){
        return replenishmentRepository.save(replenishment);
    }

    @PostMapping("/cryptoReplenishmentSession/save")
    public Mono<CryptoReplenishmentSession> saveCryptoReplenishmentSession(@RequestBody CryptoReplenishmentSession cryptoReplenishmentSession){
        return cryptoReplenishmentSessionRepository.save(cryptoReplenishmentSession);
    }

    @DeleteMapping("/cryptoReplenishmentSession/deleteCryptoReplenishmentSessionByUserIdAndCurrency")
    public Mono<Boolean> deleteCryptoReplenishmentSessionById(@RequestParam("userId") long userId,
                                                           @RequestParam("currency") CryptoReplenishmentSessionCurrency currency){
        return cryptoReplenishmentSessionRepository.deleteCryptoReplenishmentSessionByUserIdAndCurrency(userId, currency);
    }

}
