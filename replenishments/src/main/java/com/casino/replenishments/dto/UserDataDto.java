package com.casino.replenishments.dto;

import com.casino.replenishments.enums.UserDataProfileType;
import com.casino.replenishments.enums.UserDataRank;
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
public class UserDataDto {
    private UserDataProfileType profileType;
    private String nickname;
    private Long balance;
    private UserDataRank rank;
    private String serverSeed;
    private String clientSeed;
}
