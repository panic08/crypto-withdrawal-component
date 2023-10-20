package com.casino.auth.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserActivity {
    private Long id;
//    @JsonIgnore
    private Long userId;
    private String ipAddress;
    private String browserName;
    private String operatingSystem;
    private String browserVersion;
    private Long timestamp;
}
