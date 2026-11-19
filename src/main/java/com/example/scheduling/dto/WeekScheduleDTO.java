package com.example.scheduling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeekScheduleDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    private List<ScheduleCellDTO> cells;

    private List<IssueDTO> issues;

    /** DRAFT / PUBLISHED */
    private String status = "DRAFT";

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;
}
