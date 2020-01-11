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
package no.priv.bang.servlet.frontend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;

import no.priv.bang.osgi.service.mocks.logservice.MockLogService;

class FrontendServletTest {

    @Test
    void testGet() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockLogService logservice = new MockLogService();
        FrontendServlet servlet = new FrontendServlet();
        servlet.setLogService(logservice);

        servlet.doGet(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("text/html", response.getContentType());
        String responseBody = response.getOutputStreamContent();
        assertThat(responseBody).contains("bundle.js");
    }

    @Test
    void testGetRedirectToSlash() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURL("http://localhost:8181/someapp");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockLogService logservice = new MockLogService();
        FrontendServlet servlet = new FrontendServlet();
        servlet.setLogService(logservice);

        servlet.doGet(request, response);
        assertEquals(302, response.getStatus());
    }

    @Test
    void testGetNotFound() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURL("http://localhost:8181/someapp");
        request.setPathInfo("/notfound.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockLogService logservice = new MockLogService();
        FrontendServlet servlet = new FrontendServlet();
        servlet.setLogService(logservice);

        servlet.doGet(request, response);
        assertEquals(404, response.getErrorCode());
    }

    @Test
    void testGetResponseThrowsIOException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURL("http://localhost:8181/someapp");
        request.setPathInfo("/");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class, CALLS_REAL_METHODS);
        response.resetAll();
        ServletOutputStream streamThrowingIOException = mock(ServletOutputStream.class);
        doThrow(IOException.class).when(streamThrowingIOException).write(anyInt());
        when(response.getOutputStream()).thenReturn(streamThrowingIOException);
        MockLogService logservice = new MockLogService();
        FrontendServlet servlet = new FrontendServlet();
        servlet.setLogService(logservice);

        servlet.doGet(request, response);
        assertEquals(500, response.getStatus());
    }

    @Test
    void testGuessContentTypeFromResourceName() {
        FrontendServlet servlet = new FrontendServlet();
        assertEquals("application/javascript", servlet.guessContentTypeFromResourceName("bundle.js"));
        assertEquals("text/css", servlet.guessContentTypeFromResourceName("application.css"));
        assertNull(servlet.guessContentTypeFromResourceName("unknown.type"));
    }

    @Test
    void testSetRoutes() {
        FrontendServlet servlet = new FrontendServlet();

        assertEquals(2, servlet.getRoutes().size());
        servlet.setRoutes("/", "/addstore", "/statistics", "/login");
        assertEquals(4, servlet.getRoutes().size());
    }

}
