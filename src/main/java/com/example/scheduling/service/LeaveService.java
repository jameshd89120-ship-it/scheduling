package com.example.scheduling.service;

import com.example.scheduling.dto.LeaveRequestDTO;
import com.example.scheduling.entity.Availability;
import com.example.scheduling.entity.Employee;
import com.example.scheduling.entity.LeaveRequest;
import com.example.scheduling.entity.Schedule;
import com.example.scheduling.repository.AvailabilityRepository;
import com.example.scheduling.repository.EmployeeRepository;
import com.example.scheduling.repository.LeaveRequestRepository;
import com.example.scheduling.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRepo;
    private final EmployeeRepository employeeRepo;
    private final AvailabilityRepository availabilityRepo;
    private final ScheduleRepository scheduleRepo;

    public List<LeaveRequestDTO> listAll() {
        Map<Long, String> names = nameMap();
        List<LeaveRequestDTO> out = new ArrayList<>();
        for (LeaveRequest l : leaveRepo.findAllByOrderByCreatedAtDesc()) {
            out.add(toDto(l, names.get(l.getEmployeeId())));
        }
        return out;
    }

    public long countPending() {
        long n = 0;
        for (LeaveRequest l : leaveRepo.findAll()) {
            if ("PENDING".equals(l.getStatus())) n++;
        }
        return n;
    }

    @Transactional
    public LeaveRequestDTO create(LeaveRequest body) {
        if (body.getEmployeeId() == null || body.getStartDate() == null || body.getEndDate() == null) {
            throw new RuntimeException("员工与请假日期不能为空");
        }
        if (body.getEndDate().isBefore(body.getStartDate())) {
            throw new RuntimeException("结束日期不能早于开始日期");
        }
        employeeRepo.findById(body.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("员工不存在"));
        LeaveRequest l = new LeaveRequest();
        l.setEmployeeId(body.getEmployeeId());
        l.setStartDate(body.getStartDate());
        l.setEndDate(body.getEndDate());
        l.setType(body.getType() == null || body.getType().trim().isEmpty() ? "PERSONAL" : body.getType());
        l.setReason(body.getReason());
        l.setStatus("PENDING");
        l.setCreatedAt(LocalDateTime.now());
        return toDto(leaveRepo.save(l), nameOf(l.getEmployeeId()));
    }

    @Transactional
    public LeaveRequestDTO approve(Long id) {
        LeaveRequest l = leaveRepo.findById(id).orElseThrow(() -> new RuntimeException("请假单不存在"));
        if (!"PENDING".equals(l.getStatus())) {
            throw new RuntimeException("只能审批待处理的请假单");
        }
        l.setStatus("APPROVED");
        l.setReviewedAt(LocalDateTime.now());
        leaveRepo.save(l);
        applyApprovedLeave(l);
        return toDto(l, nameOf(l.getEmployeeId()));
    }

    @Transactional
    public LeaveRequestDTO reject(Long id) {
        LeaveRequest l = leaveRepo.findById(id).orElseThrow(() -> new RuntimeException("请假单不存在"));
        if (!"PENDING".equals(l.getStatus())) {
            throw new RuntimeException("只能审批待处理的请假单");
        }
        l.setStatus("REJECTED");
        l.setReviewedAt(LocalDateTime.now());
        return toDto(leaveRepo.save(l), nameOf(l.getEmployeeId()));
    }

    @Transactional
    public void cancel(Long id) {
        LeaveRequest l = leaveRepo.findById(id).orElseThrow(() -> new RuntimeException("请假单不存在"));
        if ("APPROVED".equals(l.getStatus())) {
            throw new RuntimeException("已通过的请假请另行调整可用时间");
        }
        l.setStatus("CANCELLED");
        leaveRepo.save(l);
    }

    /** 已批准请假覆盖：这些日期视为不可用 */
    public Map<Long, java.util.Set<LocalDate>> approvedLeaveDates(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        Map<Long, java.util.Set<LocalDate>> map = new HashMap<>();
        for (LeaveRequest l : leaveRepo.findOverlapping(weekStart, weekEnd, "APPROVED")) {
            LocalDate d = l.getStartDate().isBefore(weekStart) ? weekStart : l.getStartDate();
            LocalDate end = l.getEndDate().isAfter(weekEnd) ? weekEnd : l.getEndDate();
            while (!d.isAfter(end)) {
                map.computeIfAbsent(l.getEmployeeId(), k -> new java.util.HashSet<>()).add(d);
                d = d.plusDays(1);
            }
        }
        return map;
    }

    private void applyApprovedLeave(LeaveRequest l) {
        LocalDate d = l.getStartDate();
        while (!d.isAfter(l.getEndDate())) {
            Schedule exist = scheduleRepo.findByEmployeeIdAndScheduleDate(l.getEmployeeId(), d);
            if (exist != null) {
                scheduleRepo.delete(exist);
            }
            LocalDate weekStart = d.with(DayOfWeek.MONDAY);
            int dayOfWeek = d.getDayOfWeek().getValue();
            Availability a = availabilityRepo.findByEmployeeIdAndWeekStartAndDayOfWeek(
                    l.getEmployeeId(), weekStart, dayOfWeek);
            if (a == null) {
                a = new Availability();
                a.setEmployeeId(l.getEmployeeId());
                a.setWeekStart(weekStart);
                a.setDayOfWeek(dayOfWeek);
            }
            a.setAvailable(false);
            availabilityRepo.save(a);
            d = d.plusDays(1);
        }
    }

    private Map<Long, String> nameMap() {
        Map<Long, String> map = new HashMap<>();
        for (Employee e : employeeRepo.findAll()) {
            map.put(e.getId(), e.getName());
        }
        return map;
    }

    private String nameOf(Long id) {
        return employeeRepo.findById(id).map(Employee::getName).orElse("未知");
    }

    private LeaveRequestDTO toDto(LeaveRequest l, String name) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(l.getId());
        dto.setEmployeeId(l.getEmployeeId());
        dto.setEmployeeName(name);
        dto.setStartDate(l.getStartDate());
        dto.setEndDate(l.getEndDate());
        dto.setType(l.getType());
        dto.setReason(l.getReason());
        dto.setStatus(l.getStatus());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setReviewedAt(l.getReviewedAt());
        return dto;
    }
}
