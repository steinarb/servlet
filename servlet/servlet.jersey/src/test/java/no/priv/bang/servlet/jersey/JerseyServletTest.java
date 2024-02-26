/*
 * Copyright 2019-2024 Steinar Bang
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.glassfish.jersey.server.ServerProperties;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;

import no.priv.bang.osgi.service.mocks.logservice.MockLogService;
import no.priv.bang.servlet.jersey.test.ExampleJerseyServlet;
import no.priv.bang.servlet.jersey.test.HelloService;

class JerseyServletTest {

    @Test
    void testGet() throws Exception {
        var request = buildGetUrl("hello");
        var response = new MockHttpServletResponse();
        var logservice = new MockLogService();
        var servlet = new ExampleJerseyServlet();
        servlet.setLogService(logservice);
        var hello = mock(HelloService.class);
        var helloString = "Hello world!";
        when(hello.hello()).thenReturn(helloString);
        servlet.setHelloService(hello);
        servlet.activate();
        var config = createEmptServletConfig();
        servlet.init(config);

        servlet.service(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        var responseBody = response.getOutputStreamContent();
        assertThat(responseBody).contains(helloString);
    }

    @Test
    void testGetWithResourcePackageSetInConfig() throws Exception {
        MockHttpServletRequest request = buildGetUrl("hi");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockLogService logservice = new MockLogService();
        ExampleJerseyServlet servlet = new ExampleJerseyServlet();
        servlet.setLogService(logservice);
        HelloService hello = mock(HelloService.class);
        String helloString = "Hi there world!";
        when(hello.hello()).thenReturn(helloString);
        servlet.setHelloService(hello);
        servlet.activate();
        ServletConfig config = createServletConfigWithApplicationAndPackagenameForJerseyResources();
        servlet.init(config);

        servlet.service(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        String responseBody = response.getOutputStreamContent();
        assertThat(responseBody).contains(helloString);
    }

    private MockHttpServletRequest buildGetUrl(String resource) {
        MockHttpServletRequest request = buildRequest(resource);
        request.setMethod("GET");
        return request;
    }

    private MockHttpServletRequest buildRequest(String resource) {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setProtocol("HTTP/1.1");
        request.setRequestURL("http://localhost:8181/" + resource);
        request.setRequestURI("/" + resource);
        request.setContextPath("");
        request.setServletPath("");
        request.setSession(session);
        return request;
    }

    private ServletConfig createEmptServletConfig() {
        ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameterNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getContextPath()).thenReturn("");
        when(config.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttributeNames()).thenReturn(Collections.emptyEnumeration());
        return config;
    }

    private ServletConfig createServletConfigWithApplicationAndPackagenameForJerseyResources() {
        ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameterNames()).thenReturn(Collections.enumeration(Arrays.asList(ServerProperties.PROVIDER_PACKAGES)));
        when(config.getInitParameter(ServerProperties.PROVIDER_PACKAGES)).thenReturn("no.priv.bang.servlet.jersey.test.webapiresources");
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getContextPath()).thenReturn("");
        when(config.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttributeNames()).thenReturn(Collections.emptyEnumeration());
        return config;
    }

}
