package me.martiandreamer.adapter;


import me.martiandreamer.model.CheckStatus;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

@RegisterRestClient(configKey = "mytime")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface MyTimeAdapter {
    @GET
    @Path("/getAccessTokenOfAnEmployee")
    List<String> getAccessTokenOfAnEmployee(@RestQuery("ldap") String ldap);

    @GET
    @Path("/checkInOut")
    String checkInOut(@RestQuery("empId") String id, @RestQuery("token") String token);

    @GET
    @Path("/getCurrentStatus")
    CheckStatus getCurrentStatus(@RestQuery("empId") String id);

}
