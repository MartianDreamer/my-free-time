package me.martiandreamer.util;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

@ApplicationScoped
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PublicOffDay {

    private static int compensation = 0;

    public static boolean isPublicOffDay() {
        return isPublicHoliday(1, Month.JANUARY)
                || isPublicHoliday(30, Month.APRIL)
                || isPublicHoliday(1, Month.MAY)
                || isSeptember2nd()
                || isSaturdayOrSunday()
                || isCompensationDay();
    }

    private static boolean isSaturdayOrSunday() {
        return LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY) || LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

    private static boolean isPublicHoliday(int day, Month month) {
        LocalDate today = LocalDate.now();
        if (today.getDayOfMonth() == day && today.getMonth().equals(month)) {
            if (isSaturdayOrSunday()) {
                compensation++;
            }
            return true;
        }
        return false;
    }

    private static boolean isSeptember2nd() {
        LocalDate today = LocalDate.now();
        if (today.getDayOfMonth() == 1 && today.getMonth().equals(Month.SEPTEMBER)) {
            if (today.getDayOfWeek().equals(DayOfWeek.MONDAY) || today.getDayOfWeek().equals(DayOfWeek.THURSDAY)) {
                return true;
            }
            if (today.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                compensation++;
                return true;
            }
            return false;
        }
        if (today.getDayOfMonth() == 2 && today.getMonth().equals(Month.SEPTEMBER)) {
            if (isSaturdayOrSunday()) {
                compensation++;
            }
            return true;
        }
        if (today.getDayOfMonth() == 3 && today.getMonth().equals(Month.SEPTEMBER)) {
            if (today.getDayOfWeek().equals(DayOfWeek.FRIDAY) || today.getDayOfWeek().equals(DayOfWeek.TUESDAY)) {
                return true;
            }
            if (today.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                compensation++;
                return true;
            }
            return false;
        }
        return false;
    }

    private static boolean isCompensationDay() {
        if (compensation > 0) {
            compensation--;
            return true;
        }
        return false;
    }
}
