package com.example.scheduling.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "scheduling.defaults")
public class SchedulingProperties {
    private String openTime = "08:30";
    private String closeTime = "23:30";
    private Integer needPerSlot = 2;
    private Double workHours = 8.0;
    private Double breakHours = 1.5;
    private Integer maxDaysPerWeek = 5;
    private Integer maxHoursPerDay = 120;
    // 🚨 新增餐期参数
    private String lunchStart = "10:30";
    private String lunchEnd = "13:30";
    private Integer lunchNeed = 4;     // 午餐期需要的人数

    private String dinnerStart = "16:30";
    private String dinnerEnd = "19:30";
    private Integer dinnerNeed = 4;    // 晚餐期需要的人数
}