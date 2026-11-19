package com.example.scheduling.repository;

import com.example.scheduling.entity.ScheduleWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ScheduleWeekRepository extends JpaRepository<ScheduleWeek, Long> {
    Optional<ScheduleWeek> findByWeekStart(LocalDate weekStart);
}
