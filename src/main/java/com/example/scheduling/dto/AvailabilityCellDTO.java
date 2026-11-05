package com.example.scheduling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailabilityCellDTO {

    private Long employeeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    private Integer dayOfWeek;

    private Boolean available;
}