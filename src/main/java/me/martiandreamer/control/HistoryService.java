package me.martiandreamer.control;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import me.martiandreamer.model.CheckInCheckOutTime;
import me.martiandreamer.model.CheckStatus;
import me.martiandreamer.util.TimeCalculation;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static me.martiandreamer.util.PublicOffDay.isPublicOffDay;

@ApplicationScoped
@RequiredArgsConstructor
public class HistoryService {
    private final CommunicationService communicationService;
    private final AbsentDayService absentDayService;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final List<CheckInCheckOutTime> historyQueue = new LinkedList<>();

    @SuppressWarnings("ReassignedVariable")
    @PostConstruct
    public void scheduleUpdate() {
        LocalDateTime at1130pm = LocalDate.now().atTime(23, 30);
        LocalDateTime now = LocalDateTime.now();
        long durationBetween = Duration.between(now, at1130pm).toSeconds();
        if (durationBetween < 0) {
            writeHistory();
            LocalDateTime tomorrow1130pm = LocalDate.now().plusDays(1).atTime(23, 30);
            durationBetween = Duration.between(now, tomorrow1130pm).toSeconds();
            scheduledExecutorService.scheduleAtFixedRate(this::writeHistory, durationBetween, 24 * 60 * 60, TimeUnit.SECONDS);
        }
        scheduledExecutorService.scheduleAtFixedRate(this::writeHistory, durationBetween, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    private void writeHistory() {
        if (absentDayService.isAbsentDay() || isPublicOffDay()) {
            return;
        }
        CheckStatus checkStatus = communicationService.checkStatus();
        if (checkStatus.intime() == 0) {
            return;
        }
        CheckInCheckOutTime checkInCheckOutTime = TimeCalculation.calculateWorkingHour(checkStatus.intime(), checkStatus.outtime());
        if (historyQueue.size() == 7) {
            historyQueue.removeLast();
        }
        historyQueue.addFirst(checkInCheckOutTime);
    }

    public List<CheckInCheckOutTime> readHistory() {
        return historyQueue;
    }

    public List<List<String>> readStringHistory() {
        return historyQueue.stream()
                .map(CheckInCheckOutTime::toStringList)
                .toList();
    }
}
