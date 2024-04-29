package me.martiandreamer.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.martiandreamer.model.CheckInCheckOutTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeCalculation {

    @SuppressWarnings("ReassignedVariable")
    public static CheckInCheckOutTime calculateWorkingHour(long intime, long outtime) {
        LocalDateTime checkin = LocalDate.now().atStartOfDay().plusSeconds(intime);
        LocalDateTime checkout = LocalDate.now().atStartOfDay().plusSeconds(outtime);
        LocalDateTime at1930pm = LocalDate.now().atTime(19, 30);
        if (checkout.isAfter(at1930pm)) {
            checkout = at1930pm;
        }
        double totalWorkingHour = (outtime - intime - 5400) / 3600d;
        if (checkin.isAfter(LocalDate.now().atTime(10, 0))) {
            long at130pm = LocalTime.of(13, 30).toSecondOfDay();
            totalWorkingHour = (outtime - at130pm) / 3600d;
        }
        if (checkout.isBefore(LocalDate.now().atTime(13, 30))) {
            long at12pm = LocalTime.of(12, 0).toSecondOfDay();
            totalWorkingHour = (at12pm - intime) / 3600d;
        }
        return new CheckInCheckOutTime(checkin, checkout, totalWorkingHour);
    }

}
