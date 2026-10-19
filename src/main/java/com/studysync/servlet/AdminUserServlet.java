package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.User;
import com.studysync.service.AdminService;
import com.studysync.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller enabling administrators to manage and moderate student user accounts.
 * 
 * CONCEPT: Admin Security & Account Moderation
 * WHY: Provides account activation/deactivation while strictly preventing an admin
 * from deactivating their own active account.
 */
@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        List<User> users = adminService.listAllUsers();
        req.setAttribute("users", users);
        req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
        req.setAttribute("flashError", FlashUtil.getError(session));
        req.getRequestDispatcher("/WEB-INF/views/admin/adminUsers.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentAdmin = (User) session.getAttribute(AuthFilter.SESSION_USER);

        String action = req.getParameter("action");
        if ("toggleStatus".equalsIgnoreCase(action)) {
            try {
                int targetUserId = Integer.parseInt(req.getParameter("targetUserId"));
                boolean newStatus = adminService.toggleUserStatus(targetUserId, currentAdmin.getUserId());
                FlashUtil.setSuccess(req, "User account status successfully updated to " + (newStatus ? "ACTIVE" : "INACTIVE") + ".");
            } catch (NumberFormatException nfe) {
                FlashUtil.setError(req, "Invalid user ID format.");
            } catch (StudySyncException se) {
                FlashUtil.setError(req, se.getMessage());
            }
        } else {
            FlashUtil.setError(req, "Unknown administration action.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }
}
