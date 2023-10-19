package com.casino.auth.dto.steam;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SteamUserDto {
    private Response response;
    @Getter
    @Setter
    public static class Response{
        private Players[] players;
    }

    @JsonNaming(PropertyNamingStrategies.LowerCaseStrategy.class)
    @Getter
    @Setter
    public static class Players{
        private String steamId;
        private String personaName;
        private String avatarFull;
    }
}
