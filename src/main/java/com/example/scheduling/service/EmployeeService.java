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
        return repo.findAllByOrderByStatusAscNameAsc();
    }

    public List<Employee> findActive() {
        return repo.findByStatusOrderByNameAsc("ACTIVE");
    }

    public Employee save(Employee e) {
        if (e.getStatus() == null || e.getStatus().trim().isEmpty()) {
            e.setStatus("ACTIVE");
        }
        if (e.getDepartment() == null || e.getDepartment().trim().isEmpty()) {
            e.setDepartment("门店");
        }
        return repo.save(e);
    }

    public Employee update(Long id, Employee body) {
        Employee e = repo.findById(id).orElseThrow(() -> new RuntimeException("员工不存在"));
        e.setName(body.getName());
        e.setPosition(body.getPosition());
        e.setDepartment(body.getDepartment());
        e.setPhone(body.getPhone());
        e.setMaxHoursPerWeek(body.getMaxHoursPerWeek());
        e.setStatus(body.getStatus() == null || body.getStatus().trim().isEmpty()
                ? "ACTIVE" : body.getStatus());
        e.setHireDate(body.getHireDate());
        e.setNote(body.getNote());
        return repo.save(e);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
