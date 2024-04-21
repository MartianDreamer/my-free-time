package me.martiandreamer.control;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.martiandreamer.model.CheckInCheckOutTime;
import me.martiandreamer.model.CheckStatus;
import me.martiandreamer.model.Configuration;
import me.martiandreamer.util.TimeCalculation;

import java.time.Duration;
import java.time.LocalDateTime;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MailerService {
    private final Mailer mailer;
    private final Configuration configuration;
    private final CommunicationService communicationService;

    public void sendCheckinMail() {
        if (configuration.getEmail() == null) {
            return;
        }
        String now = LocalDateTime.now().format(ScheduledCheckService.DATE_TIME_FORMATTER);
        try {
            mailer.send(
                    Mail.withText(configuration.getEmail(),
                            "NO REPLY - You checked in at " + now + ".",
                            "Dear " + communicationService.getWindowsAccount() + ",\n" +
                                    "You checked in at " + now + ".\n\n" +
                                    "Regards,\n" +
                                    "Your free time.")
            );
        } catch (RuntimeException e) {
            log.error("failed to send check in mail", e);
        }
    }

    public void sendCheckoutMail(CheckStatus checkStatus) {
        if (configuration.getEmail() == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        long outtime = Duration.between(startOfDay, now).toSeconds();
        String nowString = now.format(ScheduledCheckService.DATE_TIME_FORMATTER);
        CheckInCheckOutTime checkInCheckOutTime = TimeCalculation.calculateWorkingHour(checkStatus.intime(), outtime);
        try {
            mailer.send(
                    Mail.withText(configuration.getEmail(),
                            "NO REPLY - You checked out at " + nowString + ".",
                            "Dear " + communicationService.getWindowsAccount() + ",\n" +
                                    "You checked out at " + nowString + ".\n" +
                                    "You worked for " + checkInCheckOutTime.totalWorkingHour() + ".\n\n" +
                                    "Regards,\n" +
                                    "Your free time.")
            );
        } catch (RuntimeException e) {
            log.error("failed to send check out mail", e);
        }
    }

}
