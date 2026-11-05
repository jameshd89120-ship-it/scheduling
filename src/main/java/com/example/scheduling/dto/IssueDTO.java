package com.example.scheduling.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class IssueDTO {

    /** "error" 或 "warning" */
    private String level;

    private String message;

    private Long employeeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}