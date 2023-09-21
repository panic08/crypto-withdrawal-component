package com.casino.replenishmentapi.api;

import com.casino.replenishmentapi.dto.ReplenishmentDto;
import com.casino.replenishmentapi.model.ReplenishmentData;
import com.casino.replenishmentapi.repository.ReplenishmentDataRepository;
import com.casino.replenishmentapi.repository.ReplenishmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8084")
@RequiredArgsConstructor
public class ReplenishmentApi {

    private final ReplenishmentRepository replenishmentRepository;
    private final ReplenishmentDataRepository replenishmentDataRepository;

    @GetMapping("/replenishment/findAllReplenishment")
    public Flux<ReplenishmentDto> getAllReplenishment(
            @RequestParam("userId") long userId,
            @RequestParam("limit") int limit,
            @RequestParam("startIndex") int startIndex,
            @RequestParam("endIndex") int endIndex
    ){
        return replenishmentRepository.findAllByUserIdByCreatedAtDesc(userId, limit)
                .flatMap(replenishment -> {
                    Mono<ReplenishmentData> replenishmentDataMono =
                            replenishmentDataRepository.findById(replenishment.getId());

                    ReplenishmentDto replenishmentDto = new ReplenishmentDto();

                    replenishmentDto.setId(replenishment.getId());
                    replenishmentDto.setUserId(replenishment.getUserId());
                    replenishmentDto.setType(replenishment.getType());
                    replenishmentDto.setCurrency(replenishment.getCurrency());
                    replenishmentDto.setAmount(replenishment.getAmount());
                    replenishmentDto.setCreatedAt(replenishment.getCreatedAt());

                    return replenishmentDataMono.flatMap(replenishmentData -> {
                        replenishmentDto.setReplenishmentData(replenishmentData);

                        return Mono.just(replenishmentDto);
                    });
                })
                .skip(startIndex)
                .take(endIndex - startIndex + 1);
    }
}
