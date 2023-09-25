package com.casino.replenishments.mapper;

import com.casino.replenishments.dto.CryptoReplenishmentMessage;
import com.casino.replenishments.payload.CryptoReplenishmentTrxRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CryptoReplenishmentTrxRequestToCryptoReplenishmentMessageMapper {
    @Mappings({
            @Mapping(ignore = true, target = "id"),
            @Mapping(ignore = true, target = "userId"),
            @Mapping(ignore = true, target = "recipientAddress"),
            @Mapping(ignore = true, target = "recipientPrivateKey"),
            @Mapping(ignore = true, target = "recipientPublicKey"),
            @Mapping(constant = "TRX", target = "currency"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(source = "data.ipAddress", target = "data.ipAddress"),
            @Mapping(source = "data.browserName", target = "data.browserName"),
            @Mapping(source = "data.operatingSystem", target = "data.operatingSystem"),
            @Mapping(source = "data.browserVersion", target = "data.browserVersion"),
            @Mapping(ignore = true, target = "untilTimestamp")
    })
   CryptoReplenishmentMessage cryptoReplenishmentTrxRequestToCryptoReplenishmentMessage(CryptoReplenishmentTrxRequest cryptoReplenishmentTrxRequest);
}
