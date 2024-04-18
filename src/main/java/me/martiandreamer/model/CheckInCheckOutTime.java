package me.martiandreamer.model;

import java.time.LocalDateTime;

public record CheckInCheckOutTime(LocalDateTime checkinAt, LocalDateTime checkoutAt, double totalWorkingHour) {
}
