/*
 * Copyright (c) 2000-2019 TeamDev. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

package io.spine.quickstart.web;

import com.google.common.net.HttpHeaders;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

/**
 * A {@link javax.servlet.Filter} which appends the CORS headers to the HTTP responses.
 */
@WebFilter(filterName = "CORS filter", urlPatterns = "*")
public final class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        boolean inputParametersAcceptable = request instanceof HttpServletRequest
                && response instanceof HttpServletResponse;
        checkState(inputParametersAcceptable);

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        appendHeaders(httpRequest, httpResponse);
        chain.doFilter(httpRequest, httpResponse);
    }

    /**
     * {@inheritDoc}
     *
     * <p>There is no need to take actions upon the filter initialization.
     */
    @Override
    public void init(FilterConfig filterConfig) {
        // NOP.
    }

    /**
     * {@inheritDoc}
     *
     * <p>There is no need to take actions upon the filter destruction.
     */
    @Override
    public void destroy() {
        // NOP.
    }

    private static void appendHeaders(HttpServletRequest httpRequest,
                                      HttpServletResponse response) {
        String origin = httpRequest.getHeader(HttpHeaders.ORIGIN);
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, CONTENT_TYPE);
    }
}
