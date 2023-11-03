package com.casino.replenishments.model;

import com.casino.replenishments.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
