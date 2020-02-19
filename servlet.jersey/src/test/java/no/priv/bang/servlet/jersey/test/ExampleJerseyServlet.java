package no.priv.bang.servlet.jersey.test;

import javax.servlet.Servlet;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import no.priv.bang.servlet.jersey.JerseyServlet;

@Component(service={Servlet.class})
public class ExampleJerseyServlet extends JerseyServlet {
    private static final long serialVersionUID = -675612245532839390L;

    @Reference
    public void setHelloService(HelloService service) {
        addInjectedOsgiService(HelloService.class, service);
    }

    @Activate
    public void activate() {
        // Called when all injections are satisfied
    }
}
