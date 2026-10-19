package com.studysync.filter;

import com.studysync.util.CsrfUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filter enforcing CSRF token validation on all state-changing HTTP POST requests.
 * 
 * CONCEPT: CSRF Defense Architecture
 * WHY:
 * 1. For GET requests: Ensures a cryptographically strong session token is seeded for all form views.
 * 2. For POST requests: Validates incoming token using constant-time comparison (thwarting timing attacks).
 * 3. Special handling for Multipart forms (/notes/upload): Standard HttpServletRequest cannot parse
 *    multipart body parameters in a filter prior to servlet @MultipartConfig binding, so token is validated
 *    either via query param or directly within the upload servlet.
 */
public class CsrfFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Ensure session has a valid token for form rendering
        HttpSession session = httpRequest.getSession(true);
        CsrfUtil.getOrCreateToken(session);

        String method = httpRequest.getMethod();

        if ("POST".equalsIgnoreCase(method)) {
            String uri = httpRequest.getRequestURI();
            String contentType = httpRequest.getContentType();

            // Handle multipart/form-data: parameters are parsed by NoteUploadServlet via @MultipartConfig
            boolean isMultipart = contentType != null && contentType.toLowerCase().startsWith("multipart/");
            boolean isUploadEndpoint = uri.endsWith("/notes/upload");

            if (isMultipart && isUploadEndpoint) {
                // Check if token was provided in query string; otherwise servlet inspects multipart part
                String queryToken = httpRequest.getParameter(CsrfUtil.CSRF_PARAM_NAME);
                if (queryToken != null && !CsrfUtil.isValidToken(httpRequest)) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
                    return;
                }
                chain.doFilter(request, response);
                return;
            }

            // Standard form-urlencoded POST validation
            if (!CsrfUtil.isValidToken(httpRequest)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
