package me.martiandreamer.control;

import me.martiandreamer.model.CheckStatus;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Slf4j
class CommunicationServiceTest {

    @Inject
    CommunicationService communicationService;

    @Test
    void checkStatus() {
        CheckStatus checkStatus = communicationService.checkStatus();
        log.info("{}", checkStatus);
        assertNotNull(checkStatus);
    }
}