package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.Rating;
import com.studysync.model.StudyRequest;
import com.studysync.model.StudySession;
import com.studysync.model.StudySessionSummary;
import com.studysync.model.User;
import com.studysync.service.RatingService;
import com.studysync.service.SessionSummaryService;
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
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller managing study sessions (scheduling, viewing, cancelling, completing,
 * and binary serialization summary export/import).
 * 
 * CONCEPT: MVC, Transactions & Serialization Integration
 * WHY: Coordinates session state transitions and integrates the SessionSummaryService
 * binary snapshot serialization feature.
 */
@WebServlet("/sessions")
public class SessionServlet extends HttpServlet {

    private final StudySessionService sessionService = new StudySessionService();
    private final StudyRequestService requestService = new StudyRequestService();
    private final SessionSummaryService summaryService = new SessionSummaryService();
    private final RatingService ratingService = new RatingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);
        int currentUserId = currentUser.getUserId();

        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isBlank()) {
            // View Session Details
            try {
                int sessionId = Integer.parseInt(idParam);
                StudySession studySession = sessionService.getSessionDetails(sessionId, currentUserId);
                req.setAttribute("studySession", studySession);

                // Check if user has already rated this session
                boolean hasRated = ratingService.hasRated(sessionId, currentUserId);
                req.setAttribute("hasRated", hasRated);

                // Attempt to load serialized summary if it exists
                try {
                    StudySessionSummary summary = summaryService.loadSummary(sessionId, currentUser);
                    req.setAttribute("sessionSummary", summary);
                } catch (Exception ignored) {
                    // Summary file not exported yet - normal state
                }

                req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
                req.setAttribute("flashError", FlashUtil.getError(session));
                req.getRequestDispatcher("/WEB-INF/views/sessionDetails.jsp").forward(req, resp);
                return;
            } catch (NumberFormatException nfe) {
                FlashUtil.setError(req, "Invalid session identifier.");
                resp.sendRedirect(req.getContextPath() + "/sessions");
                return;
            } catch (StudySyncException se) {
                FlashUtil.setError(req, se.getMessage());
                resp.sendRedirect(req.getContextPath() + "/sessions");
                return;
            }
        }

        // List all sessions for user
        List<StudySession> sessions = sessionService.listSessionsForUser(currentUserId);
        req.setAttribute("sessions", sessions);

        // Also check if scheduling from a request: ?scheduleRequestId=...
        String scheduleRequestId = req.getParameter("scheduleRequestId");
        if (scheduleRequestId != null && !scheduleRequestId.isBlank()) {
            try {
                int reqId = Integer.parseInt(scheduleRequestId);
                StudyRequest reqObj = requestService.getRequestById(reqId);
                req.setAttribute("scheduleRequest", reqObj);
            } catch (Exception ignored) {}
        }

        req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
        req.setAttribute("flashError", FlashUtil.getError(session));
        req.getRequestDispatcher("/WEB-INF/views/studySessions.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);
        int currentUserId = currentUser.getUserId();

        String action = req.getParameter("action");

        try {
            if ("schedule".equalsIgnoreCase(action)) {
                int requestId = Integer.parseInt(req.getParameter("requestId"));
                String dateStr = req.getParameter("sessionDate");
                String timeStr = req.getParameter("sessionTime");

                LocalDate date = LocalDate.parse(dateStr);
                LocalTime time = LocalTime.parse(timeStr);

                StudySession created = sessionService.scheduleSession(requestId, date, time, currentUserId);
                FlashUtil.setSuccess(req, "Study session successfully scheduled for " + created.getDateDisplay() + " at " + created.getTimeDisplay() + "!");
                resp.sendRedirect(req.getContextPath() + "/sessions?id=" + created.getSessionId());
                return;
            } else if ("cancel".equalsIgnoreCase(action)) {
                int sessionId = Integer.parseInt(req.getParameter("sessionId"));
                sessionService.cancelSession(sessionId, currentUserId);
                FlashUtil.setSuccess(req, "Study session #" + sessionId + " has been cancelled.");
                resp.sendRedirect(req.getContextPath() + "/sessions?id=" + sessionId);
                return;
            } else if ("complete".equalsIgnoreCase(action)) {
                int sessionId = Integer.parseInt(req.getParameter("sessionId"));
                sessionService.markCompleted(sessionId, currentUserId);
                FlashUtil.setSuccess(req, "Session marked as completed! You can now submit a peer review rating.");
                resp.sendRedirect(req.getContextPath() + "/sessions?id=" + sessionId);
                return;
            } else if ("exportSummary".equalsIgnoreCase(action)) {
                int sessionId = Integer.parseInt(req.getParameter("sessionId"));
                Path exportedPath = summaryService.exportSummary(sessionId, currentUser);
                FlashUtil.setSuccess(req, "Session summary serialized and exported successfully! File: " + exportedPath.getFileName());
                resp.sendRedirect(req.getContextPath() + "/sessions?id=" + sessionId);
                return;
            } else {
                FlashUtil.setError(req, "Unknown session action.");
            }
        } catch (StudySyncException se) {
            FlashUtil.setError(req, se.getMessage());
        } catch (Exception e) {
            FlashUtil.setError(req, "Failed to process session request: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/sessions");
    }
}
