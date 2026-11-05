package com.example.scheduling.controller;

import com.example.scheduling.common.Result;
import com.example.scheduling.entity.Employee;
import com.example.scheduling.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @GetMapping
    public Result<List<Employee>> list() {
        return Result.success(service.findAll());
    }

    @PostMapping
    public Result<Employee> add(@RequestBody Employee e) {
        return Result.success(service.save(e));
    }

    @PutMapping("/{id}")
    public Result<Employee> update(@PathVariable Long id, @RequestBody Employee e) {
        return Result.success(service.update(id, e));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success(null);
    }
}