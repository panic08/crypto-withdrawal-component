package com.casino.replenishmentapi.model;

import com.casino.replenishmentapi.enums.ReplenishmentCurrency;
import com.casino.replenishmentapi.enums.ReplenishmentMethod;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("replenishments_table")
@Data
public class Replenishment {
    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("method")
    private ReplenishmentMethod method;

    @Column("currency")
    private ReplenishmentCurrency currency;

    @Column("amount")
    private Double amount;

    @Column("created_at")
    private Long createdAt;
}
