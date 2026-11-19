package com.example.scheduling.controller;

import com.example.scheduling.common.Result;
import com.example.scheduling.dto.LeaveRequestDTO;
import com.example.scheduling.entity.LeaveRequest;
import com.example.scheduling.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService service;

    @GetMapping
    public Result<List<LeaveRequestDTO>> list() {
        return Result.success(service.listAll());
    }

    @PostMapping
    public Result<LeaveRequestDTO> create(@RequestBody LeaveRequest body) {
        return Result.success(service.create(body));
    }

    @PostMapping("/{id}/approve")
    public Result<LeaveRequestDTO> approve(@PathVariable Long id) {
        return Result.success(service.approve(id));
    }

    @PostMapping("/{id}/reject")
    public Result<LeaveRequestDTO> reject(@PathVariable Long id) {
        return Result.success(service.reject(id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return Result.success(null);
    }
}
