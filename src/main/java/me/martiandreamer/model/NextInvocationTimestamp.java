package me.martiandreamer.model;

import java.time.LocalDateTime;

public record NextInvocationTimestamp(LocalDateTime nextCheckinAt, LocalDateTime nextCheckoutAt) {
}
