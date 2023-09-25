package com.casino.replenishmentapi.model;

import com.casino.replenishmentapi.enums.ReplenishmentCurrency;
import com.casino.replenishmentapi.enums.ReplenishmentMethod;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("replenishments_table")
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
