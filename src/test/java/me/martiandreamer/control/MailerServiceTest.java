package me.martiandreamer.control;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import me.martiandreamer.model.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@QuarkusTest
class MailerServiceTest {

    @Inject
    MailerService mailerService;
    @Inject
    Configuration configuration;

    @Test
    void doNotCrash_givenNotConfigEmailSender() {
        configuration.setEmail("no_email@nomail.com");
        assertDoesNotThrow(() -> mailerService.sendCheckMail(MailerService.Action.CHECKOUT));
    }
}