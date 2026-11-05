package com.example.scheduling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleUpdateRequest {

    private Long employeeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduleDate;

    /** 传 null 表示删除该条排班 */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private BigDecimal breakHours;

    private ScheduleSettings settings;
}