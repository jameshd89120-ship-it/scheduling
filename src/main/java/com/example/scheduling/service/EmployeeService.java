package com.example.scheduling.service;

import com.example.scheduling.entity.Employee;
import com.example.scheduling.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repo;

    public List<Employee> findAll() {
        return repo.findAll();
    }

    public Employee save(Employee e) {
        return repo.save(e);
    }

    public Employee update(Long id, Employee body) {
        Employee e = repo.findById(id).orElseThrow(() -> new RuntimeException("员工不存在"));
        e.setName(body.getName());
        e.setPosition(body.getPosition());
        e.setPhone(body.getPhone());
        e.setMaxHoursPerWeek(body.getMaxHoursPerWeek());
        return repo.save(e);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}