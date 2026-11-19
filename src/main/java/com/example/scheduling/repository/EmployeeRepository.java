package com.example.scheduling.repository;

import com.example.scheduling.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByStatusOrderByNameAsc(String status);
    List<Employee> findAllByOrderByStatusAscNameAsc();
}
