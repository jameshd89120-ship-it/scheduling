package com.example.scheduling.service;

import com.example.scheduling.config.SchedulingProperties;
import com.example.scheduling.dto.GenerateRequest;
import com.example.scheduling.dto.IssueDTO;
import com.example.scheduling.dto.ScheduleCellDTO;
import com.example.scheduling.dto.ScheduleSettings;
import com.example.scheduling.dto.ScheduleUpdateRequest;
import com.example.scheduling.dto.ScheduleUpdateResponse;
import com.example.scheduling.dto.WeekScheduleDTO;
import com.example.scheduling.entity.Availability;
import com.example.scheduling.entity.Employee;
import com.example.scheduling.entity.Schedule;
import com.example.scheduling.repository.AvailabilityRepository;
import com.example.scheduling.repository.EmployeeRepository;
import com.example.scheduling.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final EmployeeRepository employeeRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleGenerator generator;
    private final ScheduleValidator validator;
    private final SchedulingProperties props;

    /* ---------- 查询一周排班 ---------- */
    public WeekScheduleDTO getWeek(LocalDate weekStart) {
        LocalDate end = weekStart.plusDays(6);
        List<Schedule> list = scheduleRepository.findByScheduleDateBetween(weekStart, end);
        List<ScheduleCellDTO> cells = new ArrayList<>();
        for (Schedule s : list) {
            cells.add(generator.buildCell(s.getEmployeeId(), s.getScheduleDate(),
                    s.getStartTime(), s.getEndTime(), s.getBreakHours()));
        }
        ScheduleSettings settings = buildSettings(null);
        List<IssueDTO> issues = validator.validate(weekStart, settings);
        return new WeekScheduleDTO(weekStart, cells, issues);
    }

    /* ---------- 自动排班 ---------- */
    @Transactional
    public WeekScheduleDTO generate(GenerateRequest req) {
        if (req.getWeekStart() == null) throw new RuntimeException("weekStart 不能为空");
        LocalDate weekStart = req.getWeekStart();
        ScheduleSettings settings = buildSettings(req.getSettings());

        List<Employee> employees = employeeRepository.findAll();
        List<Availability> avails = availabilityRepository.findByWeekStart(weekStart);

        Map<Long, Set<LocalDate>> availMap = new HashMap<>();
        for (Availability a : avails) {
            if (Boolean.TRUE.equals(a.getAvailable())) {
                LocalDate date = weekStart.plusDays(a.getDayOfWeek() - 1);
                Set<LocalDate> set = availMap.get(a.getEmployeeId());
                if (set == null) {
                    set = new HashSet<>();
                    availMap.put(a.getEmployeeId(), set);
                }
                set.add(date);
            }
        }

        List<ScheduleCellDTO> cells = generator.generate(employees, availMap, weekStart, settings);
        saveWeekSchedule(weekStart, cells);

        List<IssueDTO> issues = validator.validate(weekStart, settings);
        return new WeekScheduleDTO(weekStart, cells, issues);
    }

    /* ---------- 手动微调单个格 ---------- */
    @Transactional
    public ScheduleUpdateResponse updateCell(ScheduleUpdateRequest req) {
        if (req.getEmployeeId() == null || req.getScheduleDate() == null) {
            throw new RuntimeException("employeeId / scheduleDate 不能为空");
        }

        Schedule exist = scheduleRepository.findByEmployeeIdAndScheduleDate(
                req.getEmployeeId(), req.getScheduleDate());

        ScheduleCellDTO cell = null;
        if (req.getStartTime() == null) {
            if (exist != null) scheduleRepository.delete(exist);
        } else {
            if (req.getEndTime() == null) throw new RuntimeException("endTime 不能为空");
            if (!req.getEndTime().isAfter(req.getStartTime())) {
                throw new RuntimeException("下班时间必须晚于上班时间");
            }
            BigDecimal bh = req.getBreakHours() != null ? req.getBreakHours()
                    : BigDecimal.valueOf(1.5);

            Schedule s = exist != null ? exist : new Schedule();
            s.setEmployeeId(req.getEmployeeId());
            s.setScheduleDate(req.getScheduleDate());
            s.setStartTime(req.getStartTime());
            s.setEndTime(req.getEndTime());
            s.setBreakHours(bh);
            scheduleRepository.save(s);

            cell = generator.buildCell(s.getEmployeeId(), s.getScheduleDate(),
                    s.getStartTime(), s.getEndTime(), s.getBreakHours());
        }

        LocalDate weekStart = req.getScheduleDate().with(DayOfWeek.MONDAY);
        ScheduleSettings settings = buildSettings(req.getSettings());
        List<IssueDTO> issues = validator.validate(weekStart, settings);
        return new ScheduleUpdateResponse(cell, issues);
    }

    /* ---------- 清空一周 ---------- */
    @Transactional
    public void clearWeek(LocalDate weekStart) {
        // 🚨 改这里：换成 deleteByDateRange
        scheduleRepository.deleteByDateRange(weekStart, weekStart.plusDays(6));
    }

    /* ---------- 校验 ---------- */
    public List<IssueDTO> validate(LocalDate weekStart, ScheduleSettings in) {
        return validator.validate(weekStart, buildSettings(in));
    }

    private void saveWeekSchedule(LocalDate weekStart, List<ScheduleCellDTO> cells) {
        // 1. 换成 deleteByDateRange
        scheduleRepository.deleteByDateRange(weekStart, weekStart.plusDays(6));

        // 2. 🚨 加上这句强制刷入数据库，确保 DELETE 先执行
        scheduleRepository.flush();

        List<Schedule> toSave = new ArrayList<>();
        for (ScheduleCellDTO c : cells) {
            Schedule s = new Schedule();
            s.setEmployeeId(c.getEmployeeId());
            s.setScheduleDate(c.getScheduleDate());
            s.setStartTime(c.getStartTime());
            s.setEndTime(c.getEndTime());
            s.setBreakHours(c.getBreakHours());
            toSave.add(s);
        }
        // 3. 再执行插入
        scheduleRepository.saveAll(toSave);
    }

    private ScheduleSettings buildSettings(ScheduleSettings in) {
        ScheduleSettings s = new ScheduleSettings();
        s.setOpenTime(pick(in == null ? null : in.getOpenTime(),
                LocalTime.parse(props.getOpenTime())));
        s.setCloseTime(pick(in == null ? null : in.getCloseTime(),
                LocalTime.parse(props.getCloseTime())));
        s.setNeedPerSlot(pick(in == null ? null : in.getNeedPerSlot(), props.getNeedPerSlot()));
        s.setWorkHours(pick(in == null ? null : in.getWorkHours(),
                BigDecimal.valueOf(props.getWorkHours())));
        s.setBreakHours(pick(in == null ? null : in.getBreakHours(),
                BigDecimal.valueOf(props.getBreakHours())));
        s.setMaxDaysPerWeek(pick(in == null ? null : in.getMaxDaysPerWeek(),
                props.getMaxDaysPerWeek()));
        s.setMaxHoursPerDay(pick(in == null ? null : in.getMaxHoursPerDay(),
                props.getMaxHoursPerDay()));
        // 🚨 新增
        s.setLunchStart(pick(in == null ? null : in.getLunchStart(), LocalTime.parse(props.getLunchStart())));
        s.setLunchEnd(pick(in == null ? null : in.getLunchEnd(), LocalTime.parse(props.getLunchEnd())));
        s.setLunchNeed(pick(in == null ? null : in.getLunchNeed(), props.getLunchNeed()));

        s.setDinnerStart(pick(in == null ? null : in.getDinnerStart(), LocalTime.parse(props.getDinnerStart())));
        s.setDinnerEnd(pick(in == null ? null : in.getDinnerEnd(), LocalTime.parse(props.getDinnerEnd())));
        s.setDinnerNeed(pick(in == null ? null : in.getDinnerNeed(), props.getDinnerNeed()));
        return s;
    }

    private <T> T pick(T v, T def) {
        return v != null ? v : def;
    }
}