package com.casino.withdrawals.model;

import com.casino.withdrawals.enums.WithdrawalMethod;
import com.casino.withdrawals.enums.WithdrawalStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("withdrawals_table")
public class Withdrawal {
    @Id
    @Column("id")
    private Long id;
    @Column("user_id")
    private Long userId;
    @Column("status")
    private WithdrawalStatus status;
    @Column("method")
    private WithdrawalMethod method;
    @Column("to")
    private String to;
    @Column("amount")
    private Long amount;
    @Column("created_at")
    private Long createdAt;
}
