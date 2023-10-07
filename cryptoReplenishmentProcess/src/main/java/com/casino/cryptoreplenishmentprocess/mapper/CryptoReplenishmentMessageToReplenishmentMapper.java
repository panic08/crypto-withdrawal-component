package com.casino.cryptoreplenishmentprocess.mapper;

import com.casino.cryptoreplenishmentprocess.dto.CryptoReplenishmentMessage;
import com.casino.cryptoreplenishmentprocess.model.Replenishment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CryptoReplenishmentMessageToReplenishmentMapper {

    @Mappings({
            @Mapping(source = "userId", target = "userId"),
            @Mapping(constant = "CRYPTO", target = "method"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(expression = "java(System.currentTimeMillis())", target = "createdAt")
    })
    Replenishment cryptoReplenishmentMessageToReplenishment(CryptoReplenishmentMessage cryptoReplenishmentMessage);
}
