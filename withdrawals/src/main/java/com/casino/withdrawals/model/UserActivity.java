package com.casino.withdrawals.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserActivity {
    private Long id;
    private Long userId;
    private String ipAddress;
    private String browserName;
    private String operatingSystem;
    private String browserVersion;
    private Long timestamp;
}
