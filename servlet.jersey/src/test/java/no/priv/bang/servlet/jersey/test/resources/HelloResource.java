package no.priv.bang.servlet.jersey.test.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import no.priv.bang.servlet.jersey.test.HelloService;

@Path("/hello")
public class HelloResource {

    @Inject
    HelloService service;

    @GET
    @Produces("text/plain")
    public String getHello() {
        return service.hello();
    }

}
