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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.shiro.web.util.WebUtils.SAVED_REQUEST_KEY;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.session.Session;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.subject.WebSubject;
import org.apache.shiro.web.util.SavedRequest;
import org.glassfish.jersey.server.ServerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;

import no.priv.bang.osgi.service.mocks.logservice.MockLogService;
import no.priv.bang.servlet.jersey.test.ExampleJerseyServlet;
import no.priv.bang.servlet.jersey.test.HelloService;

class JerseyServletTest {
    private static WebSecurityManager securitymanager;

    @BeforeEach
    void beforeEachTest() {
        removeWebSubjectFromThread();
    }

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

    @Test
    void testGetProtectedWithLoggedInUserWithRequiredRole() throws Exception {
        var request = buildGetUrl("protected");
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

        loginUser(request, response, "jad", "1ad");
        servlet.service(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        var responseBody = response.getOutputStreamContent();
        assertThat(responseBody).contains(helloString);
    }

    @Test
    void testGetProtectedWithLoggedInUserWithoutRequiredRole() throws Exception {
        var request = buildGetUrl("protected");
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

        loginUser(request, response, "jd", "johnnyBoi");
        servlet.service(request, response);
        assertEquals(403, response.getStatus());
    }

    @Test
    void testGetProtectedWithoutLoggedInUser() throws Exception {
        var request = buildGetUrl("protected");
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

        createSubjectAndBindItToThread(request, response);
        servlet.service(request, response);
        assertEquals(401, response.getStatus());
    }

    protected void loginUser(HttpServletRequest request, HttpServletResponse response, String username, String password) {
        var subject = createSubjectAndBindItToThread(request, response);
        var token = new UsernamePasswordToken(username, password.toCharArray(), true);
        subject.login(token);
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

    protected void removeWebSubjectFromThread() {
        ThreadContext.remove(ThreadContext.SUBJECT_KEY);
    }

    protected WebSubject createSubjectAndBindItToThread(HttpServletRequest request, HttpServletResponse response) {
        return createSubjectAndBindItToThread(getSecurityManager(), request, response);
    }

    protected WebSubject createSubjectAndBindItToThread(WebSecurityManager webSecurityManager, HttpServletRequest request, HttpServletResponse response) {
        var session = mock(Session.class);
        var savedRequest = new SavedRequest(request);
        when(session.getAttribute(SAVED_REQUEST_KEY)).thenReturn(savedRequest);
        var subject = (WebSubject) new WebSubject.Builder(webSecurityManager, request, response).session(session).buildSubject();
        ThreadContext.bind(subject);
        return subject;
    }

    public static WebSecurityManager getSecurityManager() {
        if (securitymanager == null) {
            var environment = new IniWebEnvironment();
            environment.setIni(Ini.fromResourcePath("classpath:test.shiro.ini"));
            environment.init();
            securitymanager = environment.getWebSecurityManager();
        }

        return securitymanager;
    }

}
