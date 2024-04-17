package me.martiandreamer.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static me.martiandreamer.model.AbsentDay.AbsentType.AFTERNOON;
import static me.martiandreamer.model.AbsentDay.AbsentType.FULL;
import static me.martiandreamer.model.AbsentDay.AbsentType.MORNING;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbsentDayTest {

    @Test
    void doReturnTrue_givenLocalDateTimeInAbsentDay() {
        LocalDateTime localDateTime = LocalDateTime.of(2024,1,1,1,1,1);
        AbsentDay absentDayFullDate = new AbsentDay(LocalDate.of(2024,1,1), FULL);
        AbsentDay absentDayMorning = new AbsentDay(LocalDate.of(2024,1,1), MORNING);
        AbsentDay absentDayAfternoon = new AbsentDay(LocalDate.of(2024,1,1), AFTERNOON);
        assertTrue(absentDayFullDate.contains(localDateTime));
        assertTrue(absentDayMorning.contains(localDateTime));
        assertFalse(absentDayAfternoon.contains(localDateTime));
    }
}