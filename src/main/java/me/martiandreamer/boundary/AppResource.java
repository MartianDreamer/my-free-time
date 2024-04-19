package me.martiandreamer.boundary;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import me.martiandreamer.control.AbsentDayService;
import me.martiandreamer.control.CommunicationService;
import me.martiandreamer.control.HistoryService;
import me.martiandreamer.control.ScheduledCheckService;
import me.martiandreamer.model.CheckStatus;
import me.martiandreamer.model.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Path("/")
@RequiredArgsConstructor
public class AppResource {

    private final Configuration configuration;
    private final ScheduledCheckService scheduledCheckService;
    private final AbsentDayService absentDayService;
    private final CommunicationService communicationService;
    private final HistoryService historyService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance app(Configuration config, ScheduledCheckService scheduledCheckService, AbsentDayService absentDayService, HistoryService historyService, String intime, String outtime, String workingHour);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Blocking
    public TemplateInstance app() {
        CheckStatus checkStatus = communicationService.checkStatus();
        String intime = LocalDate.now().atStartOfDay().plusSeconds(checkStatus.intime()).format(DATE_TIME_FORMATTER);
        String outtime = LocalDate.now().atStartOfDay().plusSeconds(checkStatus.outtime()).format(DATE_TIME_FORMATTER);
        double workingHour = (checkStatus.outtime() - checkStatus.intime()) / 3600d;
        if (workingHour < 0) {
            long now = LocalTime.now().toSecondOfDay();
            workingHour = (now - checkStatus.intime()) / 3600d;
        }
        String workingHourStr = String.format("%.2f", workingHour);
        return Templates.app(configuration, scheduledCheckService, absentDayService, historyService, intime, outtime, workingHourStr);
    }
}
