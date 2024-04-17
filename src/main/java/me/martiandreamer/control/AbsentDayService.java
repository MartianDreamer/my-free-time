package me.martiandreamer.control;

import me.martiandreamer.model.AbsentDay;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class AbsentDayService {
    private final List<AbsentDay> absentDays = new ArrayList<>(60);
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        executorService.scheduleAtFixedRate(this::removeExpiredAbsentDay, 0, 7, TimeUnit.DAYS);
    }

    public void add(AbsentDay absentDay) {
        absentDays.add(absentDay);
    }

    public void remove(AbsentDay absentDay) {
        absentDays.remove(absentDay);
    }

    public void removeExpiredAbsentDay() {
        absentDays.removeIf(e -> {
            LocalDate today = LocalDate.now();
            return e.date().isBefore(today);
        });
    }

    public boolean isNotAbsentDay() {
        LocalDateTime now = LocalDateTime.now();
        return absentDays.stream().noneMatch(e -> e.contains(now));
    }

    public Optional<AbsentDay> getAbsentDay(LocalDateTime localDateTime) {
        return absentDays.stream().filter(e -> e.contains(localDateTime)).findFirst();
    }

    public List<List<String>> getAbsentDays() {
        return absentDays.stream().map(AbsentDay::toStringList).toList();
    }

}
