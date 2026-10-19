package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.Student;
import com.studysync.model.User;
import com.studysync.service.ProfileService;
import com.studysync.util.FlashUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller managing viewing and updating the student's profile.
 * 
 * CONCEPT: MVC & Security (Ownership Verification)
 * WHY: The user ID to update is strictly retrieved from the authenticated session,
 * completely preventing insecure direct object reference (IDOR) attacks.
 */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final ProfileService profileService = new ProfileService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);

        try {
            User profile = profileService.getUserProfile(currentUser.getUserId());
            req.setAttribute("profile", profile);
            if (profile instanceof Student student) {
                req.setAttribute("student", student);
            }
            req.setAttribute("allSkills", profileService.getAllSkills());
            req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
            req.setAttribute("flashError", FlashUtil.getError(session));

            req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", "Error loading profile: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);

        String name = req.getParameter("name");
        String department = req.getParameter("department");
        String bio = req.getParameter("bio");

        try {
            profileService.updateProfile(currentUser.getUserId(), name, department, bio);
            // Refresh in-memory session user name
            currentUser.setName(name);
            currentUser.setDepartment(department);
            currentUser.setBio(bio);

            FlashUtil.setSuccess(req, "Profile updated successfully!");
            resp.sendRedirect(req.getContextPath() + "/profile");
        } catch (StudySyncException e) {
            FlashUtil.setError(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/profile");
        }
    }
}
