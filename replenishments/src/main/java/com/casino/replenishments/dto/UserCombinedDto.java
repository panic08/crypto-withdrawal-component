package com.casino.replenishments.dto;

import com.casino.replenishments.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
