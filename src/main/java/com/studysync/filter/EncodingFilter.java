package com.studysync.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter ensuring uniform UTF-8 character encoding across all incoming requests and outgoing responses.
 * 
 * CONCEPT: Servlet Filters (Request/Response Pre-processing)
 * WHY: Prevents encoding mismatches, corrupted non-ASCII characters, and ensures seamless
 * multi-language and unicode handling across form submissions and JSP views.
 */
public class EncodingFilter implements Filter {

    private static final String ENCODING = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            if (httpRequest.getCharacterEncoding() == null) {
                httpRequest.setCharacterEncoding(ENCODING);
            }
        }

        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setCharacterEncoding(ENCODING);
            // Default security headers
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Clean up filter resources
    }
}
