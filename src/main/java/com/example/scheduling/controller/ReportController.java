package com.example.scheduling.controller;

import com.example.scheduling.common.Result;
import com.example.scheduling.dto.DashboardDTO;
import com.example.scheduling.dto.HoursReportDTO;
import com.example.scheduling.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @GetMapping("/dashboard")
    public Result<DashboardDTO> dashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return Result.success(service.dashboard(weekStart));
    }

    @GetMapping("/reports/hours")
    public Result<HoursReportDTO> hours(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return Result.success(service.hoursReport(weekStart));
    }
}
