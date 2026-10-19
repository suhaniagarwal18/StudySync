package com.studysync.filter;

import com.studysync.model.User;
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
 * Filter enforcing authentication across protected application endpoints.
 * 
 * CONCEPT: Servlet Filters & Web Access Control
 * WHY: Centralizes authentication verification. Automatically redirects unauthenticated
 * guests to the login page while allowing public static assets and landing pages through.
 */
public class AuthFilter implements Filter {

    public static final String SESSION_USER = "user";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String uri = httpRequest.getRequestURI();
        String path = uri.substring(contextPath.length());

        // Check if the path is publicly accessible
        if (isPublicPath(path)) {
            // If already authenticated and accessing login or register, redirect to user dashboard
            HttpSession session = httpRequest.getSession(false);
            if (session != null && session.getAttribute(SESSION_USER) != null) {
                if ("/login".equals(path) || "/register".equals(path)) {
                    User user = (User) session.getAttribute(SESSION_USER);
                    // CONCEPT: Polymorphism (getDashboardPath returns /dashboard or /admin/dashboard)
                    httpResponse.sendRedirect(contextPath + user.getDashboardPath());
                    return;
                }
            }
            chain.doFilter(request, response);
            return;
        }

        // Verify active authenticated session
        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute(SESSION_USER) : null;

        if (user == null || !user.isActive()) {
            if (session != null && user != null && !user.isActive()) {
                session.invalidate();
            }
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        if (path == null || path.isEmpty() || "/".equals(path) || "/index.jsp".equals(path)) {
            return true;
        }
        if ("/login".equals(path) || "/register".equals(path)) {
            return true;
        }
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") || path.startsWith("/error/")) {
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
    }
}
