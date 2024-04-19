package me.martiandreamer.model;

import java.time.LocalTime;

public record CheckStatus(String previousAction,
                          String employeeName,
                          String day,
                          int intime,
                          int outtime,
                          int workedTime,
                          int morningTime,
                          int afternoonTime,
                          int lastStartBreak,
                          int lastEndBreak,
                          String informMessages,
                          int type
) {

    @SuppressWarnings("ReassignedVariable")
    public double getWorkingHour() {
        if (intime == 0) return 0;
        double workingHour = (outtime - intime - 5400) / 3600d;
        if (outtime == 0) {
            LocalTime now = LocalTime.now();
            long nowSecond = now.toSecondOfDay();
            if (now.isBefore(LocalTime.of(12, 0, 1))) {
                workingHour = (nowSecond - intime()) / 3600d;
            } else if (now.isAfter(LocalTime.of(13, 30, 0))) {
                workingHour = (nowSecond - intime() - 5400) / 3600d;
            } else {
                long at12pmSecond = LocalTime.of(12, 0, 0).toSecondOfDay();
                workingHour = (at12pmSecond - intime()) / 3600d;
            }
        }
        return workingHour;
    }
}
