package com.example.scheduling.service;

import com.example.scheduling.dto.AvailabilityCellDTO;
import com.example.scheduling.dto.WeekAvailabilityDTO;
import com.example.scheduling.entity.Availability;
import com.example.scheduling.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository repo;

    public WeekAvailabilityDTO getWeek(LocalDate weekStart) {
        List<Availability> list = repo.findByWeekStart(weekStart);
        List<AvailabilityCellDTO> cells = new ArrayList<>();
        for (Availability a : list) {
            AvailabilityCellDTO d = new AvailabilityCellDTO();
            d.setEmployeeId(a.getEmployeeId());
            d.setWeekStart(a.getWeekStart());
            d.setDayOfWeek(a.getDayOfWeek());
            d.setAvailable(a.getAvailable());
            cells.add(d);
        }
        return new WeekAvailabilityDTO(weekStart, cells);
    }

    @Transactional
    public void saveWeek(LocalDate weekStart, List<AvailabilityCellDTO> cells) {
        // 🚨 使用新写的方法
        repo.deleteByWeekStartQuery(weekStart);

        // 🚨 强制刷新
        repo.flush();

        for (AvailabilityCellDTO c : cells) {
            Availability a = new Availability();
            a.setEmployeeId(c.getEmployeeId());
            a.setWeekStart(weekStart);
            a.setDayOfWeek(c.getDayOfWeek());
            a.setAvailable(Boolean.TRUE.equals(c.getAvailable()));
            repo.save(a);
        }
    }
}