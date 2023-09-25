package com.casino.replenishments.controller;

import com.casino.replenishments.model.Replenishment;
import com.casino.replenishments.service.implement.ReplenishmentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/replenishment")
@RequiredArgsConstructor
public class ReplenishmentController {

    private final ReplenishmentServiceImpl replenishmentService;
    @GetMapping("/getAll")
    public Flux<Replenishment> getAll(@RequestHeader("Authorization") String authorization,
                       @RequestParam("startIndex") int startIndex,
                       @RequestParam("endIndex") int endIndex){
        return replenishmentService.getAllReplenishment(authorization, startIndex, endIndex);
    }
}
