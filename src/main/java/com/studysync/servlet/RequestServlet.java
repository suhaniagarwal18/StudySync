package com.studysync.servlet;

import com.studysync.exception.StudySyncException;
import com.studysync.filter.AuthFilter;
import com.studysync.model.StudyRequest;
import com.studysync.model.User;
import com.studysync.service.StudyRequestService;
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
 * Controller managing sent and received study requests and their lifecycle transitions.
 * 
 * CONCEPT: MVC & State Machine Handling
 * WHY: Processes invitation dispatch, acceptance, and rejection, converting domain rule
 * violations into user-facing flash messages.
 */
@WebServlet("/requests")
public class RequestServlet extends HttpServlet {

    private final StudyRequestService requestService = new StudyRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);

        int userId = currentUser.getUserId();
        List<StudyRequest> received = requestService.listReceivedRequests(userId);
        List<StudyRequest> sent = requestService.listSentRequests(userId);

        req.setAttribute("receivedRequests", received);
        req.setAttribute("sentRequests", sent);
        req.setAttribute("flashSuccess", FlashUtil.getSuccess(session));
        req.setAttribute("flashError", FlashUtil.getError(session));

        req.getRequestDispatcher("/WEB-INF/views/studyRequests.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute(AuthFilter.SESSION_USER);
        int currentUserId = currentUser.getUserId();

        String action = req.getParameter("action");

        try {
            if ("send".equalsIgnoreCase(action)) {
                int receiverId = Integer.parseInt(req.getParameter("receiverId"));
                String subject = req.getParameter("subject");

                requestService.sendRequest(currentUserId, receiverId, subject);
                FlashUtil.setSuccess(req, "Study buddy request sent successfully!");
            } else if ("accept".equalsIgnoreCase(action)) {
                int requestId = Integer.parseInt(req.getParameter("requestId"));
                requestService.acceptRequest(requestId, currentUserId);
                FlashUtil.setSuccess(req, "Study request accepted! You can now schedule a study session.");
            } else if ("reject".equalsIgnoreCase(action)) {
                int requestId = Integer.parseInt(req.getParameter("requestId"));
                requestService.rejectRequest(requestId, currentUserId);
                FlashUtil.setSuccess(req, "Study request declined.");
            } else {
                FlashUtil.setError(req, "Unknown request action.");
            }
        } catch (NumberFormatException nfe) {
            FlashUtil.setError(req, "Invalid request or receiver identifier.");
        } catch (StudySyncException se) {
            FlashUtil.setError(req, se.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/requests");
    }
}
