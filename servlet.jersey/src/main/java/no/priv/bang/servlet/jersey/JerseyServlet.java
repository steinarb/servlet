/*
 * Copyright 2019 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.servlet.jersey;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.WebConfig;
import org.osgi.service.log.LogService;

import no.priv.bang.osgi.service.adapters.logservice.LogServiceAdapter;

/**
 * This is a servlet that's intended to be a base class for a DS component
 * exposing a {@link Servlet} service that plugs into the OSGi web whiteboard
 * to provide a REST API based on <a href="https://jersey.github.io/">Jersey</a>.
 *
 * The servlet derives from the jersey {@link ServletContainer} and keeps a
 * Map&lt;Type, Object&gt; of injected OSGi services, the injected OSGi services
 * are added to the HK2 dependency injector, so that they can be injected
 * into Jersey resources implementing the REST API endpoints.
 *
 * There's also a property ResourcePackage that defaults to
 * the subpackage of the servlet's package.
 */
public class JerseyServlet extends ServletContainer {
    private static final long serialVersionUID = -1314568939940495758L;
    private final LogServiceAdapter logservice = new LogServiceAdapter();
    private final String defaultResourcePackage = getClass().getPackage().getName() + ".resources";
    Map<Class<?>, Object> injectedServices = new HashMap<>();

    protected void addInjectedOsgiServices(Class<?> servicetype, Object service) {
        injectedServices.put(servicetype, service);
    }

    public void setLogService(LogService logservice) {
        this.logservice.setLogService(logservice);
        addInjectedOsgiServices(LogService.class, logservice);
    }

    @Override
    protected void init(WebConfig webConfig) throws ServletException {
        super.init(webConfig);
        boolean hasProviderPackages = getConfiguration().getPropertyNames().contains(ServerProperties.PROVIDER_PACKAGES);
        ResourceConfig copyOfExistingConfig = new ResourceConfig(getConfiguration());
        copyOfExistingConfig.register(new AbstractBinder() {
                @SuppressWarnings("unchecked")
                @Override
                protected void configure() {
                    for (Entry<Class<?>, Object> injectedService : injectedServices.entrySet()) {
                        bind(injectedService.getValue()).to((Class<? super Object>) injectedService.getKey());
                    }
                }
            });
        setJerseyResourcePackagesDefaultIfNotSetElsewhere(hasProviderPackages, copyOfExistingConfig);
        reload(copyOfExistingConfig);
        Map<String, Object> configProperties = getConfiguration().getProperties();
        Set<Class<?>> classes = getConfiguration().getClasses();
        logservice.log(LogService.LOG_INFO, String.format("Handlereg Jersey servlet initialized with WebConfig, with resources: %s  and config params: %s", classes.toString(), configProperties.toString()));
    }

    private void setJerseyResourcePackagesDefaultIfNotSetElsewhere(boolean hasProviderPackages, ResourceConfig config) {
        if (!hasProviderPackages) {
            config.property(ServerProperties.PROVIDER_PACKAGES, defaultResourcePackage);
        }
    }

}
