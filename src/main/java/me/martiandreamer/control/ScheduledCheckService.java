package me.martiandreamer.control;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.martiandreamer.model.AbsentDay;
import me.martiandreamer.model.CheckInCheckOutTime;
import me.martiandreamer.model.CheckStatus;
import me.martiandreamer.model.Configuration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.martiandreamer.control.MailerService.Action.CHECKIN;
import static me.martiandreamer.control.MailerService.Action.CHECKOUT;
import static me.martiandreamer.model.AbsentDay.AbsentType.AFTERNOON;
import static me.martiandreamer.model.AbsentDay.AbsentType.MORNING;
import static me.martiandreamer.util.PublicOffDay.isPublicOffDay;

@ApplicationScoped
@RequiredArgsConstructor
public class ScheduledCheckService {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
    private final Configuration configuration;
    private final CommunicationService communicationService;
    private final AbsentDayService absentDayService;
    private final MailerService mailerService;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private ScheduledFuture<?> checkinScheduledFuture;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private ScheduledFuture<?> checkoutScheduledFuture;
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime nextCheckinInvocation;
    @Setter(AccessLevel.PRIVATE)
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
        if (!isPublicOffDay() && !absentDayService.isAbsentDay() && configuration.getCheckin()) {
            CheckStatus currentStatus = communicationService.checkStatus();
            if (currentStatus.intime() == 0) {
                communicationService.check();
                mailerService.sendCheckMail(CHECKIN);
            }
        }
        if (absentDayService.isAbsentDay()) {
            AbsentDay absentDay = absentDayService.getAbsentDay(LocalDateTime.now()).get();
            if (absentDay.type().equals(MORNING)) {
                LocalDateTime at1Pm30Today = LocalDate.now().atTime(13, 30);
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
        if (configuration.getCheckout()) {
            CheckStatus currentStatus = communicationService.checkStatus();
            if (currentStatus.intime() != 0 && (configuration.getReCheckOut() || !currentStatus.previousAction().equals(CheckStatus.CHECKOUT))) {
                communicationService.check();
                mailerService.sendCheckMail(CHECKOUT);
            }
        }
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime tomorrowInvocationTime = tomorrow.atTime(configuration.getCheckoutAfterH(), configuration.getCheckoutAfterM());
        Optional<AbsentDay> optionalAbsentTomorrow = absentDayService.getAbsentDay(tomorrowInvocationTime);
        if (!absentDayService.isAbsentDay() && !isPublicOffDay() && optionalAbsentTomorrow.isPresent()) {
            AbsentDay absentTomorrow = optionalAbsentTomorrow.get();
            if (absentTomorrow.type().equals(AFTERNOON)) {
                LocalDateTime at12PmTomorrow = tomorrow.atTime(12, 0);
                long nextInvocationDelay = calculateNextInvocationDelay(at12PmTomorrow) + randomVariant;
                setupNewScheduledTask(this::doCheckoutAndRescheduleJob, nextInvocationDelay, this::getCheckoutScheduledFuture, this::setCheckoutScheduledFuture, this::setNextCheckoutInvocation);
                return;
            }
        }
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

    private long calculateNextInvocationDelay(LocalDateTime expectedDayMoment) {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(now, expectedDayMoment).get(ChronoUnit.SECONDS);
    }

    @SuppressWarnings("ReassignedVariable")
    private long calculateNextInvocationDelay(int configuredH, int configureM) {
        LocalDate today = LocalDate.now();
        long result = calculateNextInvocationDelay(today.atTime(configuredH, configureM));
        if (result < 0) {
            result = calculateNextInvocationDelay(today.plusDays(1).atTime(configuredH, configureM));
        }
        return result;
    }

    private boolean isPerformingCheckin() {
        return checkinScheduledFuture != null && (!checkinScheduledFuture.isCancelled() && !checkinScheduledFuture.isDone());
    }

    private boolean isPerformingCheckout() {
        return checkoutScheduledFuture != null && (!checkoutScheduledFuture.isCancelled() && !checkoutScheduledFuture.isDone());
    }

    public String getNextCheckinInvocationString() {
        if (nextCheckinInvocation == null) {
            return null;
        }
        return nextCheckinInvocation.format(DATE_TIME_FORMATTER);
    }

    public String getNextCheckoutInvocationString() {
        if (nextCheckoutInvocation == null) {
            return null;
        }
        return nextCheckoutInvocation.format(DATE_TIME_FORMATTER);
    }

    public CheckInCheckOutTime getScheduledCheckTime() {
        if (nextCheckinInvocation != null && nextCheckoutInvocation != null) {
            double totalWorkingHour = Duration.between(nextCheckinInvocation, nextCheckoutInvocation).toSeconds() / 3600d;
            return new CheckInCheckOutTime(nextCheckinInvocation, nextCheckoutInvocation, totalWorkingHour);
        } else {
            return new CheckInCheckOutTime(nextCheckinInvocation, nextCheckoutInvocation, 0);
        }
    }

    public enum COMMAND {
        START, STOP
    }
}
