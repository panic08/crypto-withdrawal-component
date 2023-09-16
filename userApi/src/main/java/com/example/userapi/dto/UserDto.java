package com.example.userapi.dto;

import com.example.userapi.model.UserActivity;
import com.example.userapi.model.UserData;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private List<UserActivity> userActivity;
    private UserData userData;
    @JsonProperty("account_non_locked")
    private Boolean isAccountNonLocked;
    private Long registeredAt;
}
