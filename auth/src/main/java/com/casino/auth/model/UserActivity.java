package com.casino.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserActivity {
    private Long id;
//    @JsonIgnore
    private Long userId;
    @JsonProperty("ipaddress")
    private String ipAddress;
    private String browserName;
    private String operatingSystem;
    private String browserVersion;
    private Long timestamp;
}
