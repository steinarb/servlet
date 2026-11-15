package no.priv.bang.servlet.jersey;
/*
 * Copyright 2019-2026 Steinar Bang
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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.shiro.web.jaxrs.ShiroFeature;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String defaultResourcePackage = getClass().getPackage().getName() + ".resources";
    private final Map<Class<?>, Object> injectedServices = new HashMap<>();

    /**
     * Method for registering OSGi services with the HK2 dependency injection
     * container.
     *
     * DS components implementing the {@link Servlet} interface should call this method
     * to register injected OSGi services.
     *
     * @param servicetype the {@link Class} of the injected OSGi service
     * @param service the OSGi service to register with the HK2 dependency injection container
     */
    protected void addInjectedOsgiService(Class<?> servicetype, Object service) {
        injectedServices.put(servicetype, service);
    }

    @Override
    protected void init(WebConfig webConfig) throws ServletException {
        super.init(webConfig);
        var hasProviderPackages = getConfiguration().getPropertyNames().contains(ServerProperties.PROVIDER_PACKAGES);
        var copyOfExistingConfig = new ResourceConfig(getConfiguration());
        copyOfExistingConfig.register(new AbstractBinder() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {
                for (var injectedService : injectedServices.entrySet()) {
                    bind(injectedService.getValue()).to((Class<? super Object>) injectedService.getKey());
                }
            }
        });
        setJerseyResourcePackagesDefaultIfNotSetElsewhere(hasProviderPackages, copyOfExistingConfig);
        copyOfExistingConfig.register(ShiroFeature.class);
        reload(copyOfExistingConfig);
        var configProperties = getConfiguration().getProperties();
        var classes = getConfiguration().getClasses();
        logger.info("Jersey servlet initialized with WebConfig, with resources: {}  and config params: {}", classes.toString(), configProperties.toString());
    }

    private void setJerseyResourcePackagesDefaultIfNotSetElsewhere(boolean hasProviderPackages, ResourceConfig config) {
        if (!hasProviderPackages) {
            config.property(ServerProperties.PROVIDER_PACKAGES, defaultResourcePackage);
        }
    }

}
