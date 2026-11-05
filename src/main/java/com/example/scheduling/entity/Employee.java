package com.example.scheduling.entity;

import lombok.Data;

import javax.persistence.*;

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

    @Column(length = 20)
    private String phone;

    /** 每周最大工时，默认 40 */
    @Column(name = "max_hours_per_week")
    private Integer maxHoursPerWeek = 40;
}