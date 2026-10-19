package com.studysync.servlet;

import com.studysync.filter.AuthFilter;
import com.studysync.model.User;
import com.studysync.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller executing secure user logout.
 * 
 * CONCEPT: Session Invalidation & State Cleanup
 * WHY: Invokes user.logout() to wipe sensitive in-memory credentials from the domain model,
 * and calls session.invalidate() to release the server-side HTTP session.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(AuthFilter.SESSION_USER);
            if (user != null) {
                user.logout();
            }
            session.invalidate();
        }

        // Create fresh session strictly to display the logged out flash message
        HttpSession newSession = req.getSession(true);
        newSession.setAttribute(FlashUtil.FLASH_SUCCESS, "You have been logged out successfully.");

        resp.sendRedirect(req.getContextPath() + "/login");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Enforce POST method for state-changing logout; redirect GET to login
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
