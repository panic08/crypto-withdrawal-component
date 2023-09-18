package com.casino.auth.mapper;

import com.casino.auth.model.User;
import com.casino.auth.payload.google.GoogleAuthorizationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface GoogleAuthorizationRequestToUserMapper {
    @Mappings({
            @Mapping(ignore = true, target = "id"),
            @Mapping(ignore = true, target = "username"),
            @Mapping(ignore = true, target = "password"),
            @Mapping(constant = "true", target = "isAccountNonLocked"),
            @Mapping(expression = "java(System.currentTimeMillis())", target = "registeredAt")

    })
    User googleAuthorizationRequestToUser(GoogleAuthorizationRequest googleAuthorizationRequest);
}
