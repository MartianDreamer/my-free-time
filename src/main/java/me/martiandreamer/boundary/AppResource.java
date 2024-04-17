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
import me.martiandreamer.control.AppService;
import me.martiandreamer.control.CommunicationService;
import me.martiandreamer.model.CheckStatus;
import me.martiandreamer.model.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Path("/")
@RequiredArgsConstructor
public class AppResource {

    private final Configuration configuration;
    private final AppService appService;
    private final AbsentDayService absentDayService;
    private final CommunicationService communicationService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance app(Configuration config, AppService appService, AbsentDayService absentDayService, String intime, String outtime);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Blocking
    public TemplateInstance app() {
        CheckStatus checkStatus = communicationService.checkStatus();
        String intime = LocalDate.now().atStartOfDay().plusSeconds(checkStatus.intime()).format(DATE_TIME_FORMATTER);
        String outtime = LocalDate.now().atStartOfDay().plusSeconds(checkStatus.outtime()).format(DATE_TIME_FORMATTER);
        return Templates.app(configuration, appService, absentDayService, intime, outtime);
    }
}
