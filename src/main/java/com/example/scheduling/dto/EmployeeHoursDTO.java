package com.example.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHoursDTO {
    private Long employeeId;
    private String name;
    private String position;
    private String department;
    private int workDays;
    private BigDecimal workHours;
    private Integer maxHoursPerWeek;
}
