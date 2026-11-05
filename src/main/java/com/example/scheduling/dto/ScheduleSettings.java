package com.example.scheduling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class ScheduleSettings {

    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;

    private Integer needPerSlot;

    private BigDecimal workHours;

    private BigDecimal breakHours;

    private Integer maxDaysPerWeek;

    private Integer maxHoursPerDay;
    // 🚨 新增餐期字段
    @JsonFormat(pattern = "HH:mm")
    private LocalTime lunchStart;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime lunchEnd;
    private Integer lunchNeed;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime dinnerStart;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime dinnerEnd;
    private Integer dinnerNeed;
}