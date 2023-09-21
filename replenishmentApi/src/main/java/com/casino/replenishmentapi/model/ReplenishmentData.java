package com.casino.replenishmentapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("replenishments_data_table")
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReplenishmentData {
    @Id
    @Column("id")
    private Long id;

    @Column("replenishment_id")
    private Long replenishmentId;

    @Column("ipaddress")
    @JsonProperty("ipaddress")
    private String ipAddress;

    @Column("browser_name")
    private String browserName;

    @Column("operating_system")
    private String operatingSystem;

    @Column("browser_version")
    private String browserVersion;
}
