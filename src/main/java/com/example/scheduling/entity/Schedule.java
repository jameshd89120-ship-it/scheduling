package com.example.scheduling.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "schedule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_emp_date",
                columnNames = {"employee_id", "schedule_date"}))
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** 休息时长（小时），默认 1.5 */
    @Column(name = "break_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal breakHours = BigDecimal.valueOf(1.5);
}