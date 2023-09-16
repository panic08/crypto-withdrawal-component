package com.casino.auth.dto;

import com.casino.auth.model.UserActivity;
import com.casino.auth.model.UserData;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserDto {
    private long id;
    private String username;
    @JsonIgnore
    private String password;
    private List<UserActivity> userActivity;
    private UserData userData;
    @JsonProperty("account_non_locked")
    private boolean isAccountNonLocked;
    private long registeredAt;
}
