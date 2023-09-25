package com.casino.replenishments.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class User {
    private Long id;
    private String username;
    private String password;
    private Boolean isAccountNonLocked;
    private Long registeredAt;
}
