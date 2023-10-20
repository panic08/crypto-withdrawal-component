package com.example.userapi.model;

import com.example.userapi.enums.UserRole;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table("users_table")
@Data
public class User {
    @Id
    @Column("id")
    private Long id;

    @Column("username")
    private String username;

    @Column("password")
    private String password;

    @Column("account_non_locked")
    private Boolean isAccountNonLocked;

    @Column("role")
    private UserRole role;

    @Column("registered_at")
    private Long registeredAt;
}
