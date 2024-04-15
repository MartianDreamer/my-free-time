package me.martiandreamer.model;

import me.martiandreamer.control.AppService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record AbsentDay(LocalDateTime from, LocalDateTime to) {
    public boolean contains(LocalDateTime localDateTime) {
        return localDateTime.isBefore(to) && localDateTime.isAfter(from);
    }

    @Override
    public int hashCode() {
        return (from.format(AppService.DATE_TIME_FORMATTER) + to.format(AppService.DATE_TIME_FORMATTER)).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof AbsentDay absentDay) {
            return Objects.equals(this.toString(), absentDay.toString());
        }
        return false;
    }

    @Override
    public String toString() {
        return from.format(AppService.DATE_TIME_FORMATTER) + to.format(AppService.DATE_TIME_FORMATTER);
    }

    public List<String> toStringList() {
        return List.of(from.format(AppService.DATE_TIME_FORMATTER), to.format(AppService.DATE_TIME_FORMATTER));
    }
}
