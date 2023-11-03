package com.casino.replenishments.service.implement;

import com.casino.replenishments.api.ReplenishmentApi;
import com.casino.replenishments.api.UserApi;
import com.casino.replenishments.dto.ReplenishmentDto;
import com.casino.replenishments.dto.UserCombinedDto;
import com.casino.replenishments.enums.UserRole;
import com.casino.replenishments.exception.UnauthorizedRoleException;
import com.casino.replenishments.mapper.ReplenishmentToReplenishmentDtoMapperImpl;
import com.casino.replenishments.mapper.UserCombinedToUserCombinedDtoMapperImpl;
import com.casino.replenishments.service.ReplenishmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReplenishmentServiceImpl implements ReplenishmentService {

    private final ReplenishmentToReplenishmentDtoMapperImpl replenishmentToReplenishmentDtoMapper;
    private final UserCombinedToUserCombinedDtoMapperImpl userCombinedToUserCombinedDtoMapper;
    private final UserApi userApi;
    private final ReplenishmentApi replenishmentApi;

    @Override
    public Flux<ReplenishmentDto> getAllReplenishment(long principalId, int startIndex, int endIndex) {
        int finalEndIndex = endIndex + 1;

        return replenishmentApi.findAllReplenishmentByIdAndDescWithLimit(principalId, startIndex, finalEndIndex)
                .flatMap(replenishment -> Mono.just(replenishmentToReplenishmentDtoMapper.replenishmentToReplenishmentDto(replenishment)));
    }

    @Override
    public Flux<ReplenishmentDto> getAllReplenishmentForRole(long principalId, int startIndex, int endIndex) {
        return userApi.findUserById(principalId)
                .flatMapMany(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        int finalEndIndex = endIndex + 1;

                        return replenishmentApi.findAllReplenishmentByDescWithLimit(startIndex, finalEndIndex)
                                .flatMap(replenishment -> {
                                    ReplenishmentDto replenishmentDto =
                                            replenishmentToReplenishmentDtoMapper.replenishmentToReplenishmentDto(replenishment);

                                    return userApi.findUserCombinedById(replenishment.getUserId())
                                            .map(userCombined -> {
                                                UserCombinedDto userCombinedDto =
                                                        userCombinedToUserCombinedDtoMapper
                                                                .userCombinedToUserCombinedDto(userCombined);

                                                replenishmentDto.setUser(userCombinedDto);

                                                return replenishmentDto;
                                            });
                                });
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

    @Override
    public Flux<ReplenishmentDto> getAllReplenishmentByUserId(long principalId, long userId, int startIndex, int endIndex) {
        return userApi.findUserById(principalId)
                .flatMapMany(user -> {
                    if (user.getRole().equals(UserRole.ADMIN)){
                        int finalEndIndex = endIndex + 1;

                        return replenishmentApi.findAllReplenishmentByIdAndDescWithLimit(userId, startIndex, finalEndIndex)
                                .map(replenishmentToReplenishmentDtoMapper::replenishmentToReplenishmentDto);
                    } else {
                        return Mono.error(new UnauthorizedRoleException("You do not have enough rights"));
                    }
                });
    }

}
