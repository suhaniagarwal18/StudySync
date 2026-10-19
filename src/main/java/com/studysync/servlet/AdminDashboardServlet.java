package com.studysync.servlet;

import com.studysync.service.AdminService;
import com.studysync.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * Controller rendering the administrative metrics dashboard.
 * 
 * CONCEPT: Admin Security & Dashboard Aggregation
 * WHY: Protected by AdminFilter to guarantee that only administrative users access platform telemetry.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        Map<String, Object> metrics = adminService.getDashboardMetrics();
        req.setAttribute("metrics", metrics);

        req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
        req.setAttribute("flashError", FlashUtil.getError(session));

        req.getRequestDispatcher("/WEB-INF/views/admin/adminDashboard.jsp").forward(req, resp);
    }
}
