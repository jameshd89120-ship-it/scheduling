package com.example.scheduling.service;

import com.example.scheduling.config.SchedulingProperties;
import com.example.scheduling.dto.DashboardDTO;
import com.example.scheduling.dto.EmployeeHoursDTO;
import com.example.scheduling.dto.HoursReportDTO;
import com.example.scheduling.dto.IssueDTO;
import com.example.scheduling.dto.ScheduleSettings;
import com.example.scheduling.dto.WeekScheduleDTO;
import com.example.scheduling.entity.Employee;
import com.example.scheduling.entity.Schedule;
import com.example.scheduling.repository.EmployeeRepository;
import com.example.scheduling.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ScheduleRepository scheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ScheduleService scheduleService;
    private final LeaveService leaveService;
    private final SchedulingProperties props;

    public HoursReportDTO hoursReport(LocalDate weekStart) {
        LocalDate end = weekStart.plusDays(6);
        List<Schedule> list = scheduleRepository.findByScheduleDateBetween(weekStart, end);
        Map<Long, Employee> empMap = new HashMap<>();
        for (Employee e : employeeRepository.findAll()) {
            empMap.put(e.getId(), e);
        }

        Map<Long, EmployeeHoursDTO> hours = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Schedule s : list) {
            BigDecimal wh = workHours(s);
            total = total.add(wh);
            Employee e = empMap.get(s.getEmployeeId());
            EmployeeHoursDTO row = hours.computeIfAbsent(s.getEmployeeId(), id -> {
                EmployeeHoursDTO dto = new EmployeeHoursDTO();
                dto.setEmployeeId(id);
                dto.setName(e != null ? e.getName() : "未知");
                dto.setPosition(e != null ? e.getPosition() : null);
                dto.setDepartment(e != null ? e.getDepartment() : null);
                dto.setMaxHoursPerWeek(e != null ? e.getMaxHoursPerWeek() : null);
                dto.setWorkDays(0);
                dto.setWorkHours(BigDecimal.ZERO);
                return dto;
            });
            row.setWorkDays(row.getWorkDays() + 1);
            row.setWorkHours(row.getWorkHours().add(wh));
        }

        HoursReportDTO report = new HoursReportDTO();
        report.setWeekStart(weekStart);
        report.setTotalHours(total.setScale(1, RoundingMode.HALF_UP));
        report.setTotalShifts(list.size());
        List<EmployeeHoursDTO> rows = new ArrayList<>(hours.values());
        rows.sort(Comparator.comparing(EmployeeHoursDTO::getWorkHours).reversed());
        for (EmployeeHoursDTO r : rows) {
            r.setWorkHours(r.getWorkHours().setScale(1, RoundingMode.HALF_UP));
        }
        report.setEmployees(rows);
        return report;
    }

    public DashboardDTO dashboard(LocalDate weekStart) {
        WeekScheduleDTO week = scheduleService.getWeek(weekStart);
        HoursReportDTO hours = hoursReport(weekStart);

        DashboardDTO dto = new DashboardDTO();
        dto.setWeekStart(weekStart);
        dto.setWeekStatus(week.getStatus());
        dto.setActiveEmployees(employeeRepository.findByStatusOrderByNameAsc("ACTIVE").size());
        dto.setScheduledShifts(hours.getTotalShifts());
        dto.setTotalWorkHours(hours.getTotalHours());
        dto.setIssueCount(week.getIssues() == null ? 0 : week.getIssues().size());
        dto.setPendingLeaves((int) leaveService.countPending());
        dto.setUnderstaffedSlots(countUnderstaffed(weekStart, week.getIssues()));
        dto.setTopIssues(week.getIssues() == null ? new ArrayList<>()
                : week.getIssues().subList(0, Math.min(5, week.getIssues().size())));
        dto.setTopHours(hours.getEmployees().subList(0, Math.min(5, hours.getEmployees().size())));
        return dto;
    }

    private int countUnderstaffed(LocalDate weekStart, List<IssueDTO> issues) {
        if (issues == null) return 0;
        int n = 0;
        for (IssueDTO i : issues) {
            if (i.getMessage() != null && (i.getMessage().contains("缺") || i.getMessage().contains("不足"))) {
                n++;
            } else if ("error".equalsIgnoreCase(i.getLevel()) || "warning".equalsIgnoreCase(i.getLevel())) {
                n++;
            }
        }
        return n;
    }

    private BigDecimal workHours(Schedule s) {
        long total = Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
        long br = s.getBreakHours().multiply(BigDecimal.valueOf(60)).longValue();
        return BigDecimal.valueOf(total - br)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    public ScheduleSettings defaultSettings() {
        ScheduleSettings s = new ScheduleSettings();
        s.setOpenTime(LocalTime.parse(props.getOpenTime()));
        s.setCloseTime(LocalTime.parse(props.getCloseTime()));
        s.setNeedPerSlot(props.getNeedPerSlot());
        s.setWorkHours(BigDecimal.valueOf(props.getWorkHours()));
        s.setBreakHours(BigDecimal.valueOf(props.getBreakHours()));
        s.setMaxDaysPerWeek(props.getMaxDaysPerWeek());
        s.setMaxHoursPerDay(props.getMaxHoursPerDay());
        s.setLunchStart(LocalTime.parse(props.getLunchStart()));
        s.setLunchEnd(LocalTime.parse(props.getLunchEnd()));
        s.setLunchNeed(props.getLunchNeed());
        s.setDinnerStart(LocalTime.parse(props.getDinnerStart()));
        s.setDinnerEnd(LocalTime.parse(props.getDinnerEnd()));
        s.setDinnerNeed(props.getDinnerNeed());
        return s;
    }
}
