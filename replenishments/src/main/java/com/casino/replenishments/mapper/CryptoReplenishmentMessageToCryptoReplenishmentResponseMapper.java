package com.casino.replenishments.mapper;

import com.casino.replenishments.dto.CryptoReplenishmentMessage;
import com.casino.replenishments.payload.CryptoReplenishmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CryptoReplenishmentMessageToCryptoReplenishmentResponseMapper {
    @Mappings({
            @Mapping(source = "recipientAddress", target = "recipientAddress"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "untilTimestamp", target = "untilTimestamp")
    })
    CryptoReplenishmentResponse cryptoReplenishmentMessageToCryptoReplenishmentResponse(CryptoReplenishmentMessage cryptoReplenishmentMessage);
}
