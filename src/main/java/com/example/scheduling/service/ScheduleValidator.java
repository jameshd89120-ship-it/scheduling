package com.example.scheduling.service;

import com.example.scheduling.dto.IssueDTO;
import com.example.scheduling.dto.ScheduleSettings;
import com.example.scheduling.entity.Availability;
import com.example.scheduling.entity.Employee;
import com.example.scheduling.entity.Schedule;
import com.example.scheduling.repository.AvailabilityRepository;
import com.example.scheduling.repository.EmployeeRepository;
import com.example.scheduling.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ScheduleValidator {

    private static final int SLOT_MINUTES = 30;
    private static final LocalTime LATE_TIME = LocalTime.of(22, 0);

    private final EmployeeRepository employeeRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ScheduleRepository scheduleRepository;

    public List<IssueDTO> validate(LocalDate weekStart, ScheduleSettings settings) {
        List<IssueDTO> issues = new ArrayList<>();
        LocalDate endDate = weekStart.plusDays(6);

        List<Employee> employees = employeeRepository.findAll();
        List<Schedule> allSchedules = scheduleRepository.findByScheduleDateBetween(weekStart, endDate);
        List<Availability> allAvails = availabilityRepository.findByWeekStart(weekStart);

        Map<Long, Set<Integer>> availMap = new HashMap<>();
        for (Availability a : allAvails) {
            if (Boolean.TRUE.equals(a.getAvailable())) {
                Set<Integer> set = availMap.get(a.getEmployeeId());
                if (set == null) {
                    set = new HashSet<>();
                    availMap.put(a.getEmployeeId(), set);
                }
                set.add(a.getDayOfWeek());
            }
        }

        LocalTime open = settings.getOpenTime();
        LocalTime close = settings.getCloseTime();
        int openMin = open.getHour() * 60 + open.getMinute();
        int closeMin = close.getHour() * 60 + close.getMinute();
        int totalSlots = (closeMin - openMin) / SLOT_MINUTES;
        int needPerSlot = settings.getNeedPerSlot();
        BigDecimal workHours = settings.getWorkHours();
        int maxHoursPerDay = settings.getMaxHoursPerDay();
        int maxDaysPerWeek = settings.getMaxDaysPerWeek();

        Map<Long, List<Schedule>> empSchedules = new HashMap<>();
        for (Schedule s : allSchedules) {
            List<Schedule> l = empSchedules.get(s.getEmployeeId());
            if (l == null) {
                l = new ArrayList<>();
                empSchedules.put(s.getEmployeeId(), l);
            }
            l.add(s);
        }

        /* 1. 逐日：覆盖 + 每日工时上限 */
        for (int d = 0; d < 7; d++) {
            LocalDate date = weekStart.plusDays(d);
            String dayName = "周" + "一二三四五六日".charAt(d);
            int[] slots = new int[totalSlots];
            double dayWorkHours = 0;

            for (Schedule s : allSchedules) {
                if (!s.getScheduleDate().equals(date)) continue;
                if (s.getEndTime().isBefore(s.getStartTime())) continue;

                long totalMin = Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
                long breakMin = s.getBreakHours().multiply(BigDecimal.valueOf(60)).longValue();
                long workMin = totalMin - breakMin;
                if (workMin <= 0) continue;
                dayWorkHours += workMin / 60.0;

                int startSlot = (s.getStartTime().getHour() * 60 + s.getStartTime().getMinute() - openMin) / SLOT_MINUTES;
                int workSlots = (int) Math.round((double) workMin / SLOT_MINUTES);
                int breakSlots = (int) Math.round((double) breakMin / SLOT_MINUTES);
                int firstHalf = workSlots / 2;
                int secondHalf = workSlots - firstHalf;

                for (int i = 0; i < firstHalf; i++) {
                    int idx = startSlot + i;
                    if (idx >= 0 && idx < totalSlots) slots[idx]++;
                }
                for (int i = 0; i < secondHalf; i++) {
                    int idx = startSlot + firstHalf + breakSlots + i;
                    if (idx >= 0 && idx < totalSlots) slots[idx]++;
                }
            }

            int underCount = 0;
            int minOnDuty = Integer.MAX_VALUE;
            for (int i = 0; i < totalSlots; i++) {
                if (slots[i] < minOnDuty) minOnDuty = slots[i];
                if (slots[i] < needPerSlot) underCount++;
            }
            if (minOnDuty == Integer.MAX_VALUE) minOnDuty = 0;

            if (underCount > 0) {
                issues.add(new IssueDTO("warning",
                        dayName + " " + underCount + " 个时段在岗不足（最少 "
                                + minOnDuty + " 人，需求 " + needPerSlot + "）",
                        null, date));
            }
            if (dayWorkHours > maxHoursPerDay) {
                issues.add(new IssueDTO("error",
                        dayName + " 总工时 " + String.format("%.1f", dayWorkHours)
                                + "h，超过上限 " + maxHoursPerDay + "h",
                        null, date));
            }
        }

        /* 2. 逐人：可用性 / 营业边界 / 连续晚下班 / 每周天数上限 */
        for (Employee e : employees) {
            List<Schedule> list = empSchedules.get(e.getId());
            if (list == null) list = Collections.emptyList();
            int days = 0;
            LocalTime prevEnd = null;
            String prevLabel = null;

            for (int d = 0; d < 7; d++) {
                LocalDate date = weekStart.plusDays(d);
                String dayName = "周" + "一二三四五六日".charAt(d);
                Schedule s = null;
                for (Schedule x : list) {
                    if (x.getScheduleDate().equals(date)) {
                        s = x;
                        break;
                    }
                }

                if (s == null) {
                    prevEnd = null;
                    prevLabel = null;
                    continue;
                }
                days++;

                if (s.getStartTime().isBefore(open)) {
                    issues.add(new IssueDTO("error",
                            e.getName() + " " + dayName + " 上班早于营业开始", e.getId(), date));
                }
                if (s.getEndTime().isAfter(close)) {
                    issues.add(new IssueDTO("error",
                            e.getName() + " " + dayName + " 下班晚于营业结束", e.getId(), date));
                }
                if (s.getEndTime().isBefore(s.getStartTime())) {
                    issues.add(new IssueDTO("error",
                            e.getName() + " " + dayName + " 下班早于上班", e.getId(), date));
                }
                Set<Integer> avSet = availMap.get(e.getId());
                if (avSet == null || !avSet.contains(d + 1)) {
                    issues.add(new IssueDTO("error",
                            e.getName() + " " + dayName + " 不可用却被排班", e.getId(), date));
                }

                if (prevEnd != null
                        && !prevEnd.isBefore(LATE_TIME)
                        && !s.getEndTime().isBefore(LATE_TIME)) {
                    issues.add(new IssueDTO("warning",
                            e.getName() + " " + prevLabel + "→" + dayName
                                    + " 连续晚下班，建议调整", e.getId(), date));
                }
                prevEnd = s.getEndTime();
                prevLabel = dayName;
            }

            int limitDays = maxDaysPerWeek;
            if (e.getMaxHoursPerWeek() != null && e.getMaxHoursPerWeek() > 0 && workHours.intValue() > 0) {
                limitDays = Math.min(limitDays, e.getMaxHoursPerWeek() / workHours.intValue());
            }
            if (days > limitDays) {
                issues.add(new IssueDTO("error",
                        e.getName() + " 本周已排 " + days + " 天，超过上限 " + limitDays + " 天",
                        e.getId(), null));
            }
        }

        return issues;
    }
}