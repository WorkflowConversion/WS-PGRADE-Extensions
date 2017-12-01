package com.workflowconversion.rest.appdb;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/")
public class RESTService {
	@GET
	@Produces("text/html")
	public Response getStartingPage() {
		final String output = "<h1>Hello my dudes!<h1>" + "<p>RESTful Service is running ... <br>Ping @ "
				+ new Date().toString() + "</p<br>";
		return Response.status(200).entity(output).build();
	}
}
