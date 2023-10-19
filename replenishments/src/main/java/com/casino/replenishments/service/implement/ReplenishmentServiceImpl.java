package com.casino.replenishments.service.implement;

import com.casino.replenishments.model.Replenishment;
import com.casino.replenishments.property.ServicesIpProperty;
import com.casino.replenishments.service.ReplenishmentService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ReplenishmentServiceImpl implements ReplenishmentService {

    private final WebClient.Builder webClient;
    private final ServicesIpProperty servicesIpProperty;
    private static String FIND_ALL_ORIGINAL_REPLENISHMENT_BY_ID_WITH_LIMIT_URL;

    @PostConstruct
    public void init() {
        FIND_ALL_ORIGINAL_REPLENISHMENT_BY_ID_WITH_LIMIT_URL = "http://"
                + servicesIpProperty.getReplenishmentApiIp()
                + ":8083/api/replenishment/findAllOriginalReplenishmentByIdWithLimit";
    }

    @Override
    public Flux<Replenishment> getAllReplenishment(long userId, int startIndex, int endIndex) {
        int finalEndIndex = endIndex + 1;

        return findAllReplenishmentByIdWithLimit(userId, startIndex, finalEndIndex);
    }
    private Flux<Replenishment> findAllReplenishmentByIdWithLimit(long userId, int startIndex, int endIndex){
        return webClient.baseUrl(FIND_ALL_ORIGINAL_REPLENISHMENT_BY_ID_WITH_LIMIT_URL + "?userId=" + userId
        + "&startIndex=" + startIndex + "&endIndex=" + endIndex)
                .build()
                .get()
                .retrieve()
                .bodyToFlux(Replenishment.class);
    }
}
