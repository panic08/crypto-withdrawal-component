package com.casino.replenishments.mapper;

import com.casino.replenishments.model.CryptoData;
import com.casino.replenishments.payload.CryptoDataCreatePayload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CryptoDataCreatePayloadToCryptoDataMapper {
    @Mappings({
            @Mapping(source = "address", target = "address"),
            @Mapping(source = "privateKey", target = "privateKey"),
            @Mapping(source = "currency", target = "currency"),
    })
    CryptoData cryptoDataCreatePayloadToCryptoData(CryptoDataCreatePayload cryptoDataCreatePayload);
}
