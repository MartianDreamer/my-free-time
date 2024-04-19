package me.martiandreamer.control;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.martiandreamer.model.Configuration;

import java.time.LocalDateTime;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MailerService {
    private final Mailer mailer;
    private final Configuration configuration;
    private final CommunicationService communicationService;

    public void sendCheckMail(Action action) {
        if (configuration.getEmail().isEmpty()) {
            return;
        }
        String now = LocalDateTime.now().format(ScheduledCheckService.DATE_TIME_FORMATTER);
        try {
            mailer.send(
                    Mail.withText(configuration.getEmail(),
                            "You " + action.getAction() + " at " + now + ".",
                            "Dear " + communicationService.getWindowsAccount() + ",\n" +
                                    "You " + action.getAction() + " at " + now + ".\n\n" +
                                    "Regards,\n" +
                                    "Your free time.")
            );
        } catch (RuntimeException e) {
            log.error("failed to send check mail", e);
        }
    }

    @RequiredArgsConstructor
    @Getter
    public enum Action {
        CHECKIN("checked in"), CHECKOUT("checked out");

        private final String action;

    }
}
