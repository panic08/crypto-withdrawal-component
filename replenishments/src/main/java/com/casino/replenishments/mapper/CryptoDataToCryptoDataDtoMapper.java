package com.casino.replenishments.mapper;

import com.casino.replenishments.dto.CryptoDataDto;
import com.casino.replenishments.enums.CryptoDataCurrency;
import com.casino.replenishments.model.CryptoData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CryptoDataToCryptoDataDtoMapper {
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "address", target = "address"),
            @Mapping(source = "privateKey", target = "privateKey"),
            @Mapping(source = "currency", target = "currency")
    })
    CryptoDataDto cryptoDataToCryptoDataDto(CryptoData cryptoData);

}
