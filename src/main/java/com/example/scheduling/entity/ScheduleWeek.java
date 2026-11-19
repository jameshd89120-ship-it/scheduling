package com.example.scheduling.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "schedule_week",
        uniqueConstraints = @UniqueConstraint(name = "uk_week_start", columnNames = "week_start"))
public class ScheduleWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    /** DRAFT / PUBLISHED */
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
