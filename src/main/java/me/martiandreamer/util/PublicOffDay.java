package me.martiandreamer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PublicOffDay {

    public static boolean isSaturdayOrSunday() {
        return LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY) || LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

}
