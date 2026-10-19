package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.Rating;
import com.studysync.model.User;
import com.studysync.service.RatingService;
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
 * Controller managing peer feedback reviews and ratings.
 * 
 * CONCEPT: MVC & Business Validation Handling
 * WHY: Processes rating submissions, validates constraints (1-5 range, completed status,
 * non-self reviews), and feeds reviews to profile views.
 */
@WebServlet("/ratings")
public class RatingServlet extends HttpServlet {

    private final RatingService ratingService = new RatingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);

        List<Rating> ratings = ratingService.getRatingsForStudent(currentUser.getUserId());
        double avg = ratingService.getAverageRating(currentUser.getUserId());

        req.setAttribute("ratings", ratings);
        req.setAttribute("averageRating", String.format(java.util.Locale.US, "%.1f", avg));
        req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
        req.setAttribute("flashError", FlashUtil.getError(session));

        req.getRequestDispatcher("/WEB-INF/views/ratings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);
        int currentUserId = currentUser.getUserId();

        String sessionIdStr = req.getParameter("sessionId");
        String ratingValStr = req.getParameter("ratingValue");
        String feedback = req.getParameter("feedback");

        int sessionId = 0;
        try {
            sessionId = Integer.parseInt(sessionIdStr);
            int ratingValue = Integer.parseInt(ratingValStr);

            ratingService.submitRating(sessionId, currentUserId, ratingValue, feedback);
            FlashUtil.setSuccess(req, "Thank you! Your peer feedback rating has been recorded.");
            resp.sendRedirect(req.getContextPath() + "/sessions?id=" + sessionId);
        } catch (NumberFormatException nfe) {
            FlashUtil.setError(req, "Please select a valid star rating (1 to 5).");
            if (sessionId > 0) {
                resp.sendRedirect(req.getContextPath() + "/sessions?id=" + sessionId);
            } else {
                resp.sendRedirect(req.getContextPath() + "/sessions");
            }
        } catch (StudySyncException se) {
            FlashUtil.setError(req, se.getMessage());
            if (sessionId > 0) {
                resp.sendRedirect(req.getContextPath() + "/sessions?id=" + sessionId);
            } else {
                resp.sendRedirect(req.getContextPath() + "/sessions");
            }
        }
    }
}
