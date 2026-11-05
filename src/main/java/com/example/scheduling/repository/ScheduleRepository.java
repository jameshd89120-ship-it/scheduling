package com.example.scheduling.repository;

import com.example.scheduling.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByScheduleDateBetween(LocalDate start, LocalDate end);

    Schedule findByEmployeeIdAndScheduleDate(Long employeeId, LocalDate scheduleDate);

    // 🚨 注意：这里改成了 @Query，并且方法名改了
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Schedule s WHERE s.scheduleDate BETWEEN :start AND :end")
    void deleteByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}