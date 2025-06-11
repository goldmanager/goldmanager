package com.my.goldmanager.config.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.ServletException;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class UIWebFilterTest {

    private final UIWebFilter filter = new UIWebFilter();

    @Test
    void nonApiRequestIsForwarded() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/home");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals("/index.html", response.getForwardedUrl());
        // Filter chain should not have been invoked
        assertNull(chain.getRequest());
    }

    @Test
    void apiRequestIsPassedThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/items");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(response.getForwardedUrl());
        // Filter chain should receive the request
        assertEquals(request, chain.getRequest());
    }
}
