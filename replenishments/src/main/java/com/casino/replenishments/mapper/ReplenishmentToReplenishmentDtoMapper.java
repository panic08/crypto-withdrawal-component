package com.casino.replenishments.mapper;

import com.casino.replenishments.dto.ReplenishmentDto;
import com.casino.replenishments.model.Replenishment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ReplenishmentToReplenishmentDtoMapper {
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "method", target = "method"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(source = "createdAt", target = "createdAt")
    })
    ReplenishmentDto replenishmentToReplenishmentDto(Replenishment replenishment);

}
