package com.casino.cryptoreplenishmentprocess.model;

import com.casino.cryptoreplenishmentprocess.enums.UserDataProfileType;
import com.casino.cryptoreplenishmentprocess.enums.UserDataRank;
import com.casino.cryptoreplenishmentprocess.enums.UserDataRole;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserData {
    private Long id;
//    @JsonIgnore
    private Long userId;
    private UserDataProfileType profileType;
    private String nickname;
    private Long balance;
    private UserDataRole role;
    private UserDataRank rank;
    private String serverSeed;
    private String clientSeed;
}
