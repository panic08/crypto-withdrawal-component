package com.casino.replenishments.service.implement;

import com.casino.replenishments.exception.IncorrectTokenProvidedException;
import com.casino.replenishments.model.Replenishment;
import com.casino.replenishments.model.User;
import com.casino.replenishments.service.ReplenishmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReplenishmentServiceImpl implements ReplenishmentService {

    private final WebClient.Builder webClient;
    private static final String FIND_ALL_ORIGINAL_REPLENISHMENT_BY_ID_WITH_LIMIT_URL = "http://localhost:8083/api/replenishment/findAllOriginalReplenishmentByIdWithLimit";
    private static final String GET_INFO_BY_TOKEN_URL = "http://localhost:8080/api/auth/getInfoByToken";

    @Override
    public Flux<Replenishment> getAllReplenishment(String authorization, int startIndex, int endIndex) {
        return getInfoByToken(authorization.split(" ")[1])
                .onErrorResume(err -> Mono.error(new IncorrectTokenProvidedException("Incorrect token")))
                .flatMapMany(user -> {
                    int finalEndIndex = endIndex + 1;

                    return findAllReplenishmentByIdWithLimit(user.getId(), startIndex, finalEndIndex);
                });
    }

    private Mono<User> getInfoByToken(String token){
        return webClient.baseUrl(GET_INFO_BY_TOKEN_URL + "?token=" + token)
                .build()
                .get()
                .retrieve()
                .bodyToMono(User.class);
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
