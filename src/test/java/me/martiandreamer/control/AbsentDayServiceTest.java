package me.martiandreamer.control;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import me.martiandreamer.model.AbsentDay;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AbsentDayServiceTest {

    @Inject
    AbsentDayService absentDayService;

    @Test
    void isNotAbsentDay() {
        assertTrue(absentDayService.isNotAbsentDay());
        absentDayService.add(new AbsentDay(LocalDate.now(), AbsentDay.AbsentType.FULL));
        assertFalse(absentDayService.isNotAbsentDay());
    }

    @Test
    void getAbsentDay() {
        LocalDateTime time = LocalDateTime.of(2024,1,1,1,1,1);
        assertTrue(absentDayService.getAbsentDay(time).isEmpty());
        absentDayService.add(new AbsentDay(time.toLocalDate(), AbsentDay.AbsentType.FULL));
        assertTrue(absentDayService.getAbsentDay(time).isPresent());
    }
}