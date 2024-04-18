package me.martiandreamer.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record CheckInCheckOutTime(LocalDateTime checkinAt, LocalDateTime checkoutAt, double totalWorkingHour) {

    private static final DateTimeFormatter DATE_TIME_FORMATTER_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER_TIME = DateTimeFormatter.ofPattern("HH:mm");

    public List<String> toStringList() {
        return List.of(checkinAt.format(DATE_TIME_FORMATTER_DATE),
                checkinAt.format(DATE_TIME_FORMATTER_TIME),
                checkoutAt.format(DATE_TIME_FORMATTER_TIME),
                String.format("%.2f", totalWorkingHour)
        );
    }
}
