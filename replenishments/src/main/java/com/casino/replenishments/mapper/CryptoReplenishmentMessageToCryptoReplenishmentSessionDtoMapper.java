package com.casino.replenishments.mapper;

import com.casino.replenishments.dto.CryptoReplenishmentMessage;
import com.casino.replenishments.dto.CryptoReplenishmentSessionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CryptoReplenishmentMessageToCryptoReplenishmentSessionDtoMapper {
    @Mappings({
            @Mapping(source = "recipientAddress", target = "recipientAddress"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "untilTimestamp", target = "untilTimestamp")
    })
    CryptoReplenishmentSessionDto cryptoReplenishmentMessageToCryptoReplenishmentSessionDto(CryptoReplenishmentMessage cryptoReplenishmentMessage);
}
