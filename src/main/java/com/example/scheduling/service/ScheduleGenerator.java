package com.example.scheduling.service;

import com.example.scheduling.dto.ScheduleCellDTO;
import com.example.scheduling.dto.ScheduleSettings;
import com.example.scheduling.entity.Employee;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ScheduleGenerator {

    private static final int SLOT_MINUTES = 30;
    private static final LocalTime LATE_TIME = LocalTime.of(22, 0);

    public List<ScheduleCellDTO> generate(List<Employee> employees,
                                          Map<Long, Set<LocalDate>> availMap,
                                          LocalDate weekStart,
                                          ScheduleSettings settings) {

        LocalTime open = settings.getOpenTime();
        LocalTime close = settings.getCloseTime();
        double workHours = settings.getWorkHours().doubleValue();
        double breakHours = settings.getBreakHours().doubleValue();
        int needPerSlot = settings.getNeedPerSlot();
        int maxDaysPerWeek = settings.getMaxDaysPerWeek();

        int openMin = open.getHour() * 60 + open.getMinute();
        int closeMin = close.getHour() * 60 + close.getMinute();
        int totalSlots = (closeMin - openMin) / SLOT_MINUTES;
        int workSlots = (int) Math.round(workHours * 60 / SLOT_MINUTES);
        int breakSlots = (int) Math.round(breakHours * 60 / SLOT_MINUTES);
        int durSlots = workSlots + breakSlots;

        Map<Long, Integer> maxDays = new HashMap<>();
        for (Employee e : employees) {
            int limit = maxDaysPerWeek;
            if (e.getMaxHoursPerWeek() != null && e.getMaxHoursPerWeek() > 0) {
                limit = Math.min(limit, (int) Math.floor(e.getMaxHoursPerWeek() / workHours));
            }
            maxDays.put(e.getId(), Math.max(0, limit));
        }

        Map<Long, Integer> weekDays = new HashMap<>();
        Map<Long, LocalTime> prevEnd = new HashMap<>();
        List<ScheduleCellDTO> result = new ArrayList<>();

        for (int d = 0; d < 7; d++) {
            LocalDate date = weekStart.plusDays(d);
            int[] demand = new int[totalSlots];
            // Arrays.fill(demand, needPerSlot);
            for (int i = 0; i < totalSlots; i++) {
                LocalTime current = open.plusMinutes(i * 30L);

                // 判断是否在午餐期
                boolean isLunch = !current.isBefore(settings.getLunchStart()) && current.isBefore(settings.getLunchEnd());
                // 判断是否在晚餐期
                boolean isDinner = !current.isBefore(settings.getDinnerStart()) && current.isBefore(settings.getDinnerEnd());

                if (isLunch) {
                    demand[i] = settings.getLunchNeed();
                } else if (isDinner) {
                    demand[i] = settings.getDinnerNeed();
                } else {
                    demand[i] = settings.getNeedPerSlot();
                }
            }
            Set<Long> usedToday = new HashSet<>();
            Map<Long, LocalTime> todayEnd = new HashMap<>();

            while (hasDemand(demand)) {
                Employee bestEmp = null;
                int bestStart = -1;
                int bestScore = -1;
                int bestPenalty = Integer.MAX_VALUE;
                int bestWeekDays = Integer.MAX_VALUE;

                for (Employee e : employees) {
                    if (usedToday.contains(e.getId())) continue;
                    if (!availMap.getOrDefault(e.getId(), Collections.<LocalDate>emptySet()).contains(date)) continue;
                    if (weekDays.getOrDefault(e.getId(), 0) >= maxDays.getOrDefault(e.getId(), 0)) continue;

                    for (int s = 0; s + durSlots <= totalSlots; s++) {
                        int[] covered = coveredSlots(s, workSlots, breakSlots);
                        int score = 0;
                        for (int idx : covered) {
                            if (demand[idx] > 0) score++;
                        }
                        if (score == 0) continue;

                        LocalTime endT = open.plusMinutes((long) SLOT_MINUTES * (s + durSlots));
                        LocalTime prevEndT = prevEnd.get(e.getId());
                        int penalty = (prevEndT != null
                                && !prevEndT.isBefore(LATE_TIME)
                                && !endT.isBefore(LATE_TIME)) ? 1 : 0;
                        int wc = weekDays.getOrDefault(e.getId(), 0);

                        if (score > bestScore
                                || (score == bestScore && penalty < bestPenalty)
                                || (score == bestScore && penalty == bestPenalty && wc < bestWeekDays)) {
                            bestScore = score;
                            bestPenalty = penalty;
                            bestWeekDays = wc;
                            bestEmp = e;
                            bestStart = s;
                        }
                    }
                }

                if (bestEmp == null || bestScore <= 0) break;

                LocalTime startT = open.plusMinutes((long) SLOT_MINUTES * bestStart);
                LocalTime endT = startT.plusMinutes((long) SLOT_MINUTES * durSlots);
                int[] covered = coveredSlots(bestStart, workSlots, breakSlots);
                for (int idx : covered) {
                    if (demand[idx] > 0) demand[idx]--;
                }

                result.add(buildCell(bestEmp.getId(), date, startT, endT,
                        settings.getBreakHours()));

                usedToday.add(bestEmp.getId());
                todayEnd.put(bestEmp.getId(), endT);
                weekDays.put(bestEmp.getId(), weekDays.getOrDefault(bestEmp.getId(), 0) + 1);
            }

            for (Employee e : employees) {
                if (todayEnd.containsKey(e.getId())) {
                    prevEnd.put(e.getId(), todayEnd.get(e.getId()));
                } else {
                    prevEnd.remove(e.getId());
                }
            }
        }

        return result;
    }

    private int[] coveredSlots(int startSlot, int workSlots, int breakSlots) {
        int firstHalf = workSlots / 2;
        int secondHalf = workSlots - firstHalf;
        int[] arr = new int[workSlots];
        int idx = 0;
        for (int i = 0; i < firstHalf; i++) {
            arr[idx++] = startSlot + i;
        }
        int afterBreak = startSlot + firstHalf + breakSlots;
        for (int i = 0; i < secondHalf; i++) {
            arr[idx++] = afterBreak + i;
        }
        return arr;
    }

    private boolean hasDemand(int[] demand) {
        for (int d : demand) {
            if (d > 0) return true;
        }
        return false;
    }

    public ScheduleCellDTO buildCell(Long empId, LocalDate date,
                                     LocalTime start, LocalTime end, BigDecimal breakH) {
        long totalMinutes = Duration.between(start, end).toMinutes();
        long breakMinutes = breakH.multiply(BigDecimal.valueOf(60)).longValue();
        long workMinutes = totalMinutes - breakMinutes;
        long firstHalf = workMinutes / 2;

        LocalTime breakStart = start.plusMinutes(firstHalf);
        LocalTime breakEnd = breakStart.plusMinutes(breakMinutes);
        BigDecimal workHours = BigDecimal.valueOf(workMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        return new ScheduleCellDTO(empId, date, start, end, breakH,
                breakStart, breakEnd, workHours);
    }
}