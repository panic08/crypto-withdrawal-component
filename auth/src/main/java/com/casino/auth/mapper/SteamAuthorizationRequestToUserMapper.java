package com.casino.auth.mapper;

import com.casino.auth.model.User;
import com.casino.auth.payload.steam.SteamAuthorizationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface SteamAuthorizationRequestToUserMapper {
    @Mappings({
            @Mapping(ignore = true, target = "id"),
            @Mapping(ignore = true, target = "username"),
            @Mapping(ignore = true, target = "password"),
            @Mapping(constant = "true", target = "isAccountNonLocked"),
            @Mapping(constant = "DEFAULT", target = "role"),
            @Mapping(expression = "java(System.currentTimeMillis())", target = "registeredAt")

    })
    User steamAuthorizationRequestToUser(SteamAuthorizationRequest steamAuthorizationRequest);
}
