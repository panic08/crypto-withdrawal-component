package com.casino.replenishments.model;

import com.casino.replenishments.enums.UserDataProfileType;
import com.casino.replenishments.enums.UserDataRank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
    private Long id;
    private Long userId;
    private UserDataProfileType profileType;
    private String nickname;
    private Long balance;
    private UserDataRank rank;
    private String serverSeed;
    private String clientSeed;
}
