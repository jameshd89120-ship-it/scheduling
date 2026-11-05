package com.example.scheduling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class WeekScheduleDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    private List<ScheduleCellDTO> cells;

    private List<IssueDTO> issues;
}