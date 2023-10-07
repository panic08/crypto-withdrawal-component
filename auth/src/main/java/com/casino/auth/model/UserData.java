package com.casino.auth.model;

import com.casino.auth.enums.UserDataProfileType;
import com.casino.auth.enums.UserDataRank;
import com.casino.auth.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserData {
    private Long id;
//    @JsonIgnore
    private Long userId;
    private UserDataProfileType profileType;
    private String nickname;
    private Long balance;
    private UserDataRank rank;
    private String serverSeed;
    private String clientSeed;
}
