package com.example.scheduling.controller;

import com.example.scheduling.common.Result;
import com.example.scheduling.dto.GenerateRequest;
import com.example.scheduling.dto.IssueDTO;
import com.example.scheduling.dto.ScheduleSettings;
import com.example.scheduling.dto.ScheduleUpdateRequest;
import com.example.scheduling.dto.ScheduleUpdateResponse;
import com.example.scheduling.dto.WeekScheduleDTO;
import com.example.scheduling.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService service;

    @GetMapping
    public Result<WeekScheduleDTO> getWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return Result.success(service.getWeek(weekStart));
    }

    @PostMapping("/generate")
    public Result<WeekScheduleDTO> generate(@RequestBody GenerateRequest req) {
        return Result.success(service.generate(req));
    }

    @PutMapping("/cell")
    public Result<ScheduleUpdateResponse> updateCell(@RequestBody ScheduleUpdateRequest req) {
        return Result.success(service.updateCell(req));
    }

    @DeleteMapping
    public Result<Void> clear(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        service.clearWeek(weekStart);
        return Result.success(null);
    }

    @GetMapping("/validate")
    public Result<List<IssueDTO>> validate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @ModelAttribute ScheduleSettings settings) {
        return Result.success(service.validate(weekStart, settings));
    }
}