package com.casino.auth.mapper;

import com.casino.auth.dto.PublicUserCombinedDto;
import com.casino.auth.model.UserCombined;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserCombinedToPublicUserCombinedDtoMapper {
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "username", target = "username"),
            @Mapping(source = "userData", target = "userData"),
            @Mapping(source = "isAccountNonLocked", target = "accountNonLocked"),
            @Mapping(source = "role", target = "role"),
            @Mapping(source = "registeredAt", target = "registeredAt")
    })
    PublicUserCombinedDto userCombinedToPublicUserCombinedDto(UserCombined userCombined);
}
