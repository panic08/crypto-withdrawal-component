package com.example.userapi.model;

import com.example.userapi.enums.UserDataProfileType;
import com.example.userapi.enums.UserDataRank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users_data_table")
@Data
public class UserData {
    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("profile_type")
    private UserDataProfileType profileType;

    @Column("nickname")
    private String nickname;

    @Column("balance")
    private Long balance;

    @Column("rank")
    private UserDataRank rank;

    @Column("server_seed")
    private String serverSeed;

    @Column("client_seed")
    private String clientSeed;
}
