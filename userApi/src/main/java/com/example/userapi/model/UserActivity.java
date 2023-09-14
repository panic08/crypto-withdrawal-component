package com.example.userapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users_activity_table")
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserActivity {
    @Id
    @Column("id")
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("ipaddress")
    @JsonProperty("ipaddress")
    private String ipAddress;

    @Column("browser_name")
    private String browserName;

    @Column("operating_system")
    private String operatingSystem;

    @Column("browser_version")
    private String browserVersion;

    @Column("timestamp")
    private Long timestamp;
}
