/*
 * Copyright 2019-2022 Steinar Bang
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
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import static javax.servlet.http.HttpServletResponse.*;

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
        assertEquals(SC_OK, response.getStatus());
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
        assertEquals(SC_FOUND, response.getStatus());
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
        assertEquals(SC_NOT_FOUND, response.getErrorCode());
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
        assertEquals(SC_INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void testGuessContentTypeFromResourceName() {
        FrontendServlet servlet = new FrontendServlet();
        assertEquals("text/javascript", servlet.guessContentTypeFromResourceName("bundle.js"));
        assertEquals("text/css", servlet.guessContentTypeFromResourceName("application.css"));
        assertEquals("image/x-icon", servlet.guessContentTypeFromResourceName("favicon.ico"));
        assertNull(servlet.guessContentTypeFromResourceName("unknown.type"));
    }

    @Test
    void testSetRoutes() {
        FrontendServlet servlet = new FrontendServlet();

        assertEquals(2, servlet.getRoutes().size());
        servlet.setRoutes("/", "/addstore", "/statistics", "/login");
        assertEquals(4, servlet.getRoutes().size());
    }

    static class FrontendServletThatDoesProcessing extends FrontendServlet {
        private static final long serialVersionUID = -1123678063196681870L;

        @Override
        protected boolean thisIsAResourceThatShouldBeProcessed(HttpServletRequest request, String pathInfo, String resource, String contentType) {
            return true;
        }

    }

    @Test
    void testProcessResource() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockLogService logservice = new MockLogService();
        FrontendServlet servlet = new FrontendServletThatDoesProcessing();
        servlet.setLogService(logservice);

        servlet.doGet(request, response);
        assertEquals(SC_NOT_IMPLEMENTED, response.getErrorCode());
        assertEquals("text/plain", response.getContentType());
        String responseBody = response.getOutputStreamContent();
        assertThat(responseBody).contains("Processing of content not implemented");
    }

}
