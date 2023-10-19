package com.casino.replenishments.controller;

import com.casino.replenishments.model.Replenishment;
import com.casino.replenishments.service.implement.ReplenishmentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/replenishment")
@RequiredArgsConstructor
public class ReplenishmentController {

    private final ReplenishmentServiceImpl replenishmentService;
    @GetMapping("/getAll")
    public Flux<Replenishment> getAll(UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken,
                                      @RequestParam("startIndex") int startIndex,
                                      @RequestParam("endIndex") int endIndex){
        return replenishmentService.getAllReplenishment(Long.parseLong(usernamePasswordAuthenticationToken.getName()), startIndex, endIndex);
    }
}
