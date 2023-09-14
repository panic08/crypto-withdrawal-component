package com.casino.auth.model;

import com.casino.auth.enums.UserDataRank;
import com.casino.auth.enums.UserDataRole;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserData {
    private Long id;
    private Long userId;
    private String nickname;
    private Long balance;
    private UserDataRole role;
    private UserDataRank rank;
    private String serverSeed;
    private String clientSeed;
}
