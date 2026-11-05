package com.example.scheduling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GenerateRequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    private ScheduleSettings settings;
}