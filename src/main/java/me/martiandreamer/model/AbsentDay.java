package me.martiandreamer.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public record AbsentDay(LocalDate date, AbsentType type) {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    @SuppressWarnings("ReassignedVariable")
    public boolean contains(LocalDateTime localDateTime) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.atTime(23,59,59, 999_999_999);
        if (type.equals(AbsentType.MORNING)) {
            to = date.atTime(12,0,0);
        } else if (type.equals(AbsentType.AFTERNOON)) {
            from = date.atTime(12, 0, 0);
        }
        return (localDateTime.isEqual(from) || localDateTime.isAfter(from)) && localDateTime.isBefore(to);
    }

    @Override
    public int hashCode() {
        return (date.format(DATE_TIME_FORMATTER) + type.name()).hashCode();
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
        return date.format(DATE_TIME_FORMATTER) + type.name();
    }

    public List<String> toStringList() {
        return List.of(date.format(DATE_TIME_FORMATTER), type.name());
    }

    public enum AbsentType {
        FULL, MORNING, AFTERNOON
    }
}
