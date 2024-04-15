package me.martiandreamer.control;

import me.martiandreamer.model.AbsentDay;
import me.martiandreamer.model.CheckStatus;
import me.martiandreamer.model.Configuration;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApplicationScoped
@RequiredArgsConstructor
public class AppService {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
    private final Configuration configuration;
    private final CommunicationService communicationService;
    private final AbsentDayService absentDayService;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private ScheduledFuture<?> checkinScheduledFuture;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private ScheduledFuture<?> checkoutScheduledFuture;
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private LocalDateTime nextCheckinInvocation;
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private LocalDateTime nextCheckoutInvocation;

    @PostConstruct
    void init() {
        start();
    }

    public String start() {
        if (configuration.getCheckin() && !isPerformingCheckin()) {
            long randomVariant = Math.round(Math.random() * configuration.getMaxVariantInMinus() * 60L);
            long nextInvocationDelay = calculateNextInvocationDelay(configuration.getCheckinBeforeH(), configuration.getCheckinBeforeM()) + randomVariant;
            setupNewScheduledTask(this::doCheckinAndRescheduleJob, nextInvocationDelay, this::getCheckinScheduledFuture, this::setCheckinScheduledFuture, this::setNextCheckinInvocation);
        }
        if (configuration.getCheckout() && !isPerformingCheckout()) {
            long randomVariant = Math.round(Math.random() * configuration.getMaxVariantInMinus() * 60L);
            long nextInvocationDelay = calculateNextInvocationDelay(configuration.getCheckoutAfterH(), configuration.getCheckoutAfterM()) + randomVariant;
            setupNewScheduledTask(this::doCheckoutAndRescheduleJob, nextInvocationDelay, this::getCheckoutScheduledFuture, this::setCheckoutScheduledFuture, this::setNextCheckoutInvocation);
        }
        return "started!!!";
    }

    public String stop() {
        if (checkinScheduledFuture != null && !checkinScheduledFuture.isCancelled()) {
            checkinScheduledFuture.cancel(true);
            nextCheckinInvocation = null;
        }
        if (checkoutScheduledFuture != null && !checkoutScheduledFuture.isCancelled()) {
            checkoutScheduledFuture.cancel(true);
            nextCheckoutInvocation = null;
        }
        return "stopped!!!";
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void doCheckinAndRescheduleJob() {
        long randomVariant = Math.round(Math.random() * configuration.getMaxVariantInMinus() * 60L);
        if (isNotSaturdayAndSunday() && absentDayService.isNotAbsentDay()) {
            communicationService.check();
        }
        if (!absentDayService.isNotAbsentDay()) {
            AbsentDay absentDay = absentDayService.getAbsentDay(LocalDateTime.now()).get();
            LocalDateTime at1Pm30Today = LocalDate.now().atTime(13, 30);
            if (absentDay.to().isBefore(at1Pm30Today)) {
                long nextInvocationDelay = calculateNextInvocationDelay(at1Pm30Today) - randomVariant;
                setupNewScheduledTask(this::doCheckinAndRescheduleJob, nextInvocationDelay, this::getCheckinScheduledFuture, this::setCheckinScheduledFuture, this::setNextCheckinInvocation);
                return;
            }
        }
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime tomorrowInvocationTime = tomorrow.atTime(configuration.getCheckinBeforeH(), configuration.getCheckinBeforeM());
        long nextInvocationDelay = calculateNextInvocationDelay(tomorrowInvocationTime) - randomVariant;
        setupNewScheduledTask(this::doCheckinAndRescheduleJob, nextInvocationDelay, this::getCheckinScheduledFuture, this::setCheckinScheduledFuture, this::setNextCheckinInvocation);
    }

    private void doCheckoutAndRescheduleJob() {
        long randomVariant = Math.round(Math.random() * configuration.getMaxVariantInMinus() * 60L);
        if (isNotSaturdayAndSunday() && absentDayService.isNotAbsentDay()) {
            CheckStatus currentStatus = communicationService.checkStatus();
            if (currentStatus.previousAction().equals(CheckStatus.CHECKIN)) {
                communicationService.check();
            }
        }
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime tomorrowInvocationTime = tomorrow.atTime(configuration.getCheckoutAfterH(), configuration.getCheckoutAfterM());
        long nextInvocationDelay = calculateNextInvocationDelay(tomorrowInvocationTime) + randomVariant;
        setupNewScheduledTask(this::doCheckoutAndRescheduleJob, nextInvocationDelay, this::getCheckoutScheduledFuture, this::setCheckoutScheduledFuture, this::setNextCheckoutInvocation);
    }

    private void setupNewScheduledTask(Runnable runnable, long nextInvocationDelay, Supplier<ScheduledFuture<?>> scheduledFutureGetter, Consumer<ScheduledFuture<?>> scheduledFutureSetter, Consumer<LocalDateTime> nextInvocationLocalDateTimeSetter) {
        ScheduledFuture<?> currentScheduledFuture = scheduledFutureGetter.get();
        if (currentScheduledFuture != null && !currentScheduledFuture.isCancelled()) {
            currentScheduledFuture.cancel(true);
        }
        ScheduledFuture<?> newScheduledFuture = scheduledExecutorService.schedule(runnable, nextInvocationDelay, TimeUnit.SECONDS);
        scheduledFutureSetter.accept(newScheduledFuture);
        nextInvocationLocalDateTimeSetter.accept(LocalDateTime.now().plusSeconds(nextInvocationDelay));
    }

    private boolean isNotSaturdayAndSunday() {
        return !LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY) && !LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }


    private long calculateNextInvocationDelay(LocalDateTime expectedDayMoment) {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, expectedDayMoment).get(ChronoUnit.SECONDS);
    }

    private long calculateNextInvocationDelay(int configuredH, int configureM) {
        LocalDate today = LocalDate.now();
        long result = calculateNextInvocationDelay(today.atTime(configuredH, configureM));
        if (result < 0) {
            calculateNextInvocationDelay(today.plusDays(1).atTime(configuredH, configureM));
        }
        return result;
    }

    public boolean isPerformingCheckin() {
        return checkinScheduledFuture != null && (!checkinScheduledFuture.isCancelled() && !checkinScheduledFuture.isDone());
    }

    public boolean isPerformingCheckout() {
        return checkoutScheduledFuture != null && (!checkoutScheduledFuture.isCancelled() && !checkoutScheduledFuture.isDone());
    }

    public String getNextCheckinInvocationString() {
        if (nextCheckinInvocation == null) {
            return "not check in";
        }
        return nextCheckinInvocation.format(DATE_TIME_FORMATTER);
    }

    public String getNextCheckoutInvocationString() {
        if (nextCheckoutInvocation == null) {
            return "not check out";
        }
        return nextCheckoutInvocation.format(DATE_TIME_FORMATTER);
    }

    public enum COMMAND {
        START, STOP
    }
}
