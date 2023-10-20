package com.casino.auth.model;

import com.casino.auth.enums.UserRole;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCombined {
    private Long id;
    private String username;
    private String password;
    private List<UserActivity> userActivity;
    private UserData userData;
    private Boolean isAccountNonLocked;
    private UserRole role;
    private Long registeredAt;
}
