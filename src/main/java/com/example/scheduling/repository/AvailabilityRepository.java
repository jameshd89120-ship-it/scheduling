package com.example.scheduling.repository;

import com.example.scheduling.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByWeekStart(LocalDate weekStart);

    // 🚨 终极修复
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Availability a WHERE a.weekStart = :weekStart")
    void deleteByWeekStartQuery(@Param("weekStart") LocalDate weekStart);
}