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
 * Filter restricting administrative routes strictly to users with ADMIN role.
 * 
 * CONCEPT: Role-Based Access Control (RBAC) & Servlet Filters
 * WHY: Thwarts privilege escalation attacks by inspecting the user's role before permitting
 * execution of administrative servlets or rendering administrative JSP pages.
 */
public class AdminFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute(AuthFilter.SESSION_USER) : null;

        // CONCEPT: Polymorphism (Invoking getRole() on abstract User reference)
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrator role required.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
