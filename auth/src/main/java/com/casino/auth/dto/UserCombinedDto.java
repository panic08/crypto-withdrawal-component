package com.casino.auth.dto;

import com.casino.auth.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserCombinedDto {
        private long id;
        private String username;
        private List<UserActivityDto> userActivity;
        private UserDataDto userData;
        @JsonProperty("account_non_locked")
        private boolean isAccountNonLocked;
        private UserRole role;
        private long registeredAt;

}
