package com.example.scheduling.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "availability",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_emp_week_day",
                columnNames = {"employee_id", "week_start", "day_of_week"}))
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /** 该周的周一日期 */
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    /** 1=周一 ... 7=周日 */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(nullable = false)
    private Boolean available;
}