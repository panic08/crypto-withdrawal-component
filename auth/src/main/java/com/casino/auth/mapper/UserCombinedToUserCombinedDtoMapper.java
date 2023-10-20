package com.casino.auth.mapper;

import com.casino.auth.dto.UserCombinedDto;
import com.casino.auth.model.UserCombined;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserCombinedToUserCombinedDtoMapper {
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "username", target = "username"),
            @Mapping(source = "userActivity", target = "userActivity"),
            @Mapping(source = "userData", target = "userData"),
            @Mapping(source = "isAccountNonLocked", target = "accountNonLocked"),
            @Mapping(source = "role", target = "role"),
            @Mapping(source = "registeredAt", target = "registeredAt")
    })
    UserCombinedDto userCombinedToUserCombinedDto(UserCombined userCombined);
}
