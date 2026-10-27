package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.SkillType;
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
 * Controller handling adding and removing skills (KNOWN or NEEDED) for the authenticated student.
 * 
 * CONCEPT: MVC & State Changes (PRG Pattern)
 * WHY: Processes skill management requests via POST, validates cross-list business rules in ProfileService,
 * and redirects back to /profile with flash feedback.
 */
@WebServlet("/skills")
public class SkillServlet extends HttpServlet {

    private final ProfileService profileService = new ProfileService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);

        String action = req.getParameter("action");
        String skillName = req.getParameter("skillName");
        String typeParam = req.getParameter("skillType");

        try {
            SkillType type = SkillType.valueOf(typeParam.toUpperCase());

            if ("add".equalsIgnoreCase(action)) {
                profileService.addSkill(currentUser.getUserId(), skillName, type);
                FlashUtil.setSuccess(req, "Added '" + skillName.trim() + "' to your " + type.name().toLowerCase() + " skills.");
            } else if ("remove".equalsIgnoreCase(action)) {
                profileService.removeSkill(currentUser.getUserId(), skillName, type);
                FlashUtil.setSuccess(req, "Removed '" + skillName.trim() + "' from your " + type.name().toLowerCase() + " skills.");
            } else {
                FlashUtil.setError(req, "Invalid action specified.");
            }
        } catch (IllegalArgumentException iae) {
            FlashUtil.setError(req, "Invalid skill category specified.");
        } catch (StudySyncException se) {
            FlashUtil.setError(req, se.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/profile");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/profile");
    }
}


