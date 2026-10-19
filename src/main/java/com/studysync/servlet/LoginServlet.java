package com.studysync.servlet;

import com.studysync.exception.AuthenticationException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.User;
import com.studysync.service.AuthService;
import com.studysync.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller managing user authentication and secure session initialization.
 * 
 * CONCEPT: Servlets (HTTP Handling, Session Management, and Session Fixation Defense)
 * WHY: On successful authentication, rotates session ID using request.changeSessionId()
 * to protect against session fixation attacks, and redirects polymorphically based on role.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("flashSuccess", FlashUtil.getSuccess(req.getSession(false)));
        req.setAttribute("flashError", FlashUtil.getError(req.getSession(false)));
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            User user = authService.authenticate(email, password);

            // Rotate session ID to thwart session fixation
            req.changeSessionId();

            HttpSession session = req.getSession(true);
            session.setAttribute(AuthFilter.SESSION_USER, user);

            FlashUtil.setSuccess(req, "Welcome back, " + user.getName() + "!");

            // CONCEPT: Polymorphism (Redirects to /dashboard or /admin/dashboard based on dynamic type)
            resp.sendRedirect(req.getContextPath() + user.getDashboardPath());
        } catch (AuthenticationException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("email", email);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}
