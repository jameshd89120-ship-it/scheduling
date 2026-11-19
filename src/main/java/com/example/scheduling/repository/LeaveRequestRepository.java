package com.example.scheduling.repository;

import com.example.scheduling.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    @Query("SELECT l FROM LeaveRequest l WHERE l.status = :status " +
            "AND l.startDate <= :end AND l.endDate >= :start")
    List<LeaveRequest> findOverlapping(@Param("start") LocalDate start,
                                       @Param("end") LocalDate end,
                                       @Param("status") String status);
}
