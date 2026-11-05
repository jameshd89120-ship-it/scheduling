package com.example.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ScheduleUpdateResponse {

    private ScheduleCellDTO cell;

    private List<IssueDTO> issues;
}