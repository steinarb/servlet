package no.priv.bang.servlet.jersey.test.webapiresources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import no.priv.bang.servlet.jersey.test.HelloService;

@Path("/hi")
public class HiResource {

    @Inject
    HelloService service;

    @GET
    @Produces("text/plain")
    public String getHello() {
        return service.hello();
    }

}
