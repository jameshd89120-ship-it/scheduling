package com.example.scheduling.config;

import com.example.scheduling.entity.Employee;
import com.example.scheduling.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) return;
        List<Employee> list = Arrays.asList(
                create("张小雨", "前台", "13800000001", 40),
                create("李思", "收银", "13800000002", 32),
                create("王浩", "理货", "13800000003", 40),
                create("陈可", "收银", "13800000004", 24),
                create("刘一诺", "前台", "13800000005", 40),
                create("赵敏", "理货", "13800000006", 32),
                create("孙悦", "前台", "13800000007", 24),
                create("周洲", "收银", "13800000008", 40),
                create("吴迪", "理货", "13800000009", 40),
                create("郑爽", "前台", "13800000010", 40)
        );
        employeeRepository.saveAll(list);
    }

    private Employee create(String name, String pos, String phone, int maxHours) {
        Employee e = new Employee();
        e.setName(name);
        e.setPosition(pos);
        e.setPhone(phone);
        e.setMaxHoursPerWeek(maxHours);
        return e;
    }
}