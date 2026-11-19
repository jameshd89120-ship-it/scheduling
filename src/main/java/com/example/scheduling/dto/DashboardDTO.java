package com.example.scheduling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    private String weekStatus = "DRAFT";
    private int activeEmployees;
    private int scheduledShifts;
    private BigDecimal totalWorkHours = BigDecimal.ZERO;
    private int issueCount;
    private int pendingLeaves;
    private int understaffedSlots;
    private List<IssueDTO> topIssues = new ArrayList<>();
    private List<EmployeeHoursDTO> topHours = new ArrayList<>();
}
