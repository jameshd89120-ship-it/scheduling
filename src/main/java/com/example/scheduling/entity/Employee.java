package com.example.scheduling.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String position;

    @Column(length = 50)
    private String department;

    @Column(length = 20)
    private String phone;

    /** 每周最大工时，默认 40 */
    @Column(name = "max_hours_per_week")
    private Integer maxHoursPerWeek = 40;

    /**
     * ACTIVE / INACTIVE
     * 不设 nullable=false，避免已有数据的库 ADD NOT NULL 列失败
     */
    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(length = 255)
    private String note;
}
