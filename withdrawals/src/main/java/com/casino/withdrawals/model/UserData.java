package com.casino.withdrawals.model;

import com.casino.withdrawals.enums.UserDataProfileType;
import com.casino.withdrawals.enums.UserDataRank;
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
