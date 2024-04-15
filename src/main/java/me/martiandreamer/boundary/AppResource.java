package me.martiandreamer.boundary;

import me.martiandreamer.control.AbsentDayService;
import me.martiandreamer.control.AppService;
import me.martiandreamer.model.Configuration;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

@Path("/my-free-time")
@RequiredArgsConstructor
public class AppResource {

    private final Configuration configuration;
    private final AppService appService;
    private final AbsentDayService absentDayService;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance app(Configuration config, AppService appService, AbsentDayService absentDayService);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance app() {
        return Templates.app(configuration, appService, absentDayService);
    }
}
