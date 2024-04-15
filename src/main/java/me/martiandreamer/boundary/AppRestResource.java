package me.martiandreamer.boundary;

import me.martiandreamer.control.AbsentDayService;
import me.martiandreamer.control.AppService;
import me.martiandreamer.model.AbsentDay;
import me.martiandreamer.model.Configuration;
import me.martiandreamer.model.NextInvocationTimestamp;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDateTime;

@Path("/rest")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.TEXT_PLAIN)
@RequiredArgsConstructor
public class AppRestResource {

    private final Configuration configuration;
    private final AppService appService;
    private final ObjectMapper objectMapper;
    private final AbsentDayService absentDayService;

    @GET
    @Path("/config")
    public Response getConfiguration() {
        return Response.ok(configuration).build();
    }

    @POST
    @Path("/config")
    public Response configuration(String config) throws JsonProcessingException {
        Configuration configuration = objectMapper.readValue(config, Configuration.class);
        if (configuration.checkinBeforeMinuses() - configuration.checkoutAfterMinuses() > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("don't try to f*ck things up, thanks.").build();
        }
        return Response.ok(this.configuration.update(configuration)).build();
    }

    @POST
    @Path("/")
    public Response run(@RestQuery("command") @DefaultValue("START") AppService.COMMAND command) {
        return switch (command) {
            case START -> Response.ok(appService.start()).build();
            case STOP -> Response.ok(appService.stop()).build();
        };
    }

    @GET
    @Path("/next-invocation")
    public Response getNextInvocation() {
        return Response.ok(new NextInvocationTimestamp(appService.getNextCheckinInvocation(), appService.getNextCheckoutInvocation())).build();
    }

    @POST
    @Path("/absent")
    public Response addAbsent(@RestQuery("from") String from, @RestQuery("to") String to, @RestQuery("remove") @DefaultValue("false") boolean remove) {
        LocalDateTime fromDate = LocalDateTime.parse(from, AppService.DATE_TIME_FORMATTER);
        LocalDateTime toDate = LocalDateTime.parse(to, AppService.DATE_TIME_FORMATTER);
        AbsentDay absentDay = new AbsentDay(fromDate, toDate);
        if (remove) {
            absentDayService.remove(absentDay);
        } else {
            absentDayService.add(absentDay);
        }
        return Response.ok().build();
    }

    @GET
    @Path("/absent")
    public Response getAbsentDays() {
        return Response.ok(absentDayService.getAbsentDays()).build();
    }


}
