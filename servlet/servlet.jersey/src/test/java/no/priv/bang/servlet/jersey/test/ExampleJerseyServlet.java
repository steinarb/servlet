package no.priv.bang.servlet.jersey.test;

import no.priv.bang.servlet.jersey.JerseyServlet;

public class ExampleJerseyServlet extends JerseyServlet {
    private static final long serialVersionUID = -675612245532839390L;

    public void setHelloService(HelloService service) {
        addInjectedOsgiService(HelloService.class, service);
    }

    public void activate() {
        // Called when all injections are satisfied
    }
}
