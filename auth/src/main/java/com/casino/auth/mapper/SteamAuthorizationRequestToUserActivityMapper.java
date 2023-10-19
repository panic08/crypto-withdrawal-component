package com.casino.auth.mapper;

import com.casino.auth.model.UserActivity;
import com.casino.auth.payload.steam.SteamAuthorizationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface SteamAuthorizationRequestToUserActivityMapper {
    @Mappings({
            @Mapping(ignore = true, target = "id"),
            @Mapping(ignore = true, target = "userId"),
            @Mapping(source = "data.ipAddress", target = "ipAddress"),
            @Mapping(source = "data.browserName", target = "browserName"),
            @Mapping(source = "data.operatingSystem", target = "operatingSystem"),
            @Mapping(source = "data.browserVersion", target = "browserVersion"),
            @Mapping(expression = "java(System.currentTimeMillis())", target = "timestamp")
    })
    UserActivity steamAuthorizationRequestToUserActivity(SteamAuthorizationRequest steamAuthorizationRequest);
}
