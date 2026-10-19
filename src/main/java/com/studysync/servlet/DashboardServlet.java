package com.studysync.servlet;

import com.studysync.filter.AuthFilter;
import com.studysync.model.RequestStatus;
import com.studysync.model.Student;
import com.studysync.model.StudyRequest;
import com.studysync.model.StudySession;
import com.studysync.model.User;
import com.studysync.service.NoteService;
import com.studysync.service.ProfileService;
import com.studysync.service.RatingService;
import com.studysync.service.StudyRequestService;
import com.studysync.service.StudySessionService;
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
 * Controller preparing the student personalized academic dashboard.
 * 
 * CONCEPT: MVC (Controller Aggregation & View Data Preparation)
 * WHY: Gathers metric summaries (pending requests, upcoming sessions, notes count,
 * average rating) and forwards to the secured dashboard view.
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final ProfileService profileService = new ProfileService();
    private final StudySessionService sessionService = new StudySessionService();
    private final StudyRequestService requestService = new StudyRequestService();
    private final NoteService noteService = new NoteService();
    private final RatingService ratingService = new RatingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);

        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }

        try {
            int userId = currentUser.getUserId();
            User userProfile = profileService.getUserProfile(userId);
            if (userProfile instanceof Student student) {
                req.setAttribute("student", student);
            }

            List<StudySession> sessions = sessionService.listSessionsForUser(userId);
            List<StudyRequest> receivedRequests = requestService.listReceivedRequests(userId);

            long pendingCount = receivedRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .count();

            int notesCount = noteService.listNotesByUser(userId).size();
            double avgRating = ratingService.getAverageRating(userId);

            req.setAttribute("sessions", sessions);
            req.setAttribute("pendingRequestsCount", pendingCount);
            req.setAttribute("notesCount", notesCount);
            req.setAttribute("averageRating", String.format(java.util.Locale.US, "%.1f", avgRating));

            req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
            req.setAttribute("flashError", FlashUtil.getError(session));

            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", "Unable to load dashboard data: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
        }
    }
}
