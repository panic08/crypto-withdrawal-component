package com.casino.auth.mapper;

import com.casino.auth.model.User;
import com.casino.auth.payload.AuthorizationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AuthorizationRequestToUserMapper {
    @Mappings({
            @Mapping(ignore = true, target = "id"),
            @Mapping(source = "username", target = "username"),
            @Mapping(source = "password", target = "password"),
            @Mapping(constant = "true", target = "isAccountNonLocked"),
            @Mapping(expression = "java(System.currentTimeMillis())", target = "registeredAt")

    })
    User authorizationRequestToUser(AuthorizationRequest authorizationRequest);
}
