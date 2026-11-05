package com.example.scheduling.controller;

import com.example.scheduling.common.Result;
import com.example.scheduling.dto.AvailabilityCellDTO;
import com.example.scheduling.dto.WeekAvailabilityDTO;
import com.example.scheduling.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService service;

    @GetMapping
    public Result<WeekAvailabilityDTO> get(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return Result.success(service.getWeek(weekStart));
    }

    @PostMapping("/batch")
    public Result<Void> saveBatch(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestBody List<AvailabilityCellDTO> cells) {
        service.saveWeek(weekStart, cells);
        return Result.success(null);
    }
}