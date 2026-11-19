package com.example.scheduling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class HoursReportDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    private BigDecimal totalHours = BigDecimal.ZERO;
    private int totalShifts;
    private List<EmployeeHoursDTO> employees = new ArrayList<>();
}
