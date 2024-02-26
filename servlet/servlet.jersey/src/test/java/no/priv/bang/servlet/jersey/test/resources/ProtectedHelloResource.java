package no.priv.bang.servlet.jersey.test.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.authz.annotation.RequiresUser;

import no.priv.bang.servlet.jersey.test.HelloService;

@Path("/protected")
@RequiresUser
@RequiresRoles("somerole")
public class ProtectedHelloResource {

    @Inject
    HelloService service;

    @GET
    @Produces("text/plain")
    public String getHello() {
        return service.hello();
    }

}
