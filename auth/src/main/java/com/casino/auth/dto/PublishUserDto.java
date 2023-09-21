package com.casino.auth.dto;

import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.enums.UserDataRank;
import com.casino.auth.enums.UserDataRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PublishUserDto {
    private long id;
    private String username;
    private PublishUserData userData;
    @JsonProperty("account_non_locked")
    private boolean isAccountNonLocked;
    private long registeredAt;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PublishUserData{
        private Long id;
        private Long userId;
        private UserDataProfileType profileType;
        private String nickname;
        private Long balance;
        private UserDataRole role;
        private UserDataRank rank;
    }
}
