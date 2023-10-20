package com.casino.replenishments.mapper;

import com.casino.replenishments.dto.CryptoReplenishmentSessionDto;
import com.casino.replenishments.model.CryptoReplenishmentSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CryptoReplenishmentSessionToCryptoReplenishmentSessionDtoMapper {
    @Mappings({
            @Mapping(source = "recipientAddress", target = "recipientAddress"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "untilTimestamp", target = "untilTimestamp")
    })
    CryptoReplenishmentSessionDto cryptoReplenishmentSessionToCryptoReplenishmentSessionDto(CryptoReplenishmentSession cryptoReplenishmentSession);
}
