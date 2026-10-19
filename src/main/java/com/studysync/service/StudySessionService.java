package com.studysync.service;

import com.studysync.dao.StudyRequestDAO;
import com.studysync.dao.StudySessionDAO;
import com.studysync.dao.UserDAO;
import com.studysync.exception.AuthorizationException;
import com.studysync.exception.InvalidRequestException;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.exception.ValidationException;
import com.studysync.model.RequestStatus;
import com.studysync.model.SessionStatus;
import com.studysync.model.Student;
import com.studysync.model.StudyRequest;
import com.studysync.model.StudySession;
import com.studysync.model.User;
import com.studysync.util.ValidationUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service managing study session scheduling, lifecycle transitions, and participant access control.
 * 
 * CONCEPT: Object Relationships (Association Over Inheritance)
 * WHY: Orchestrates the creation of a StudySession linked by association to an accepted
 * StudyRequest. Guarantees that only authentic request participants can schedule, view,
 * cancel, or complete sessions.
 */
public class StudySessionService {

    private final StudySessionDAO studySessionDAO;
    private final StudyRequestDAO studyRequestDAO;
    private final UserDAO userDAO;

    public StudySessionService() {
        this.studySessionDAO = new StudySessionDAO();
        this.studyRequestDAO = new StudyRequestDAO();
        this.userDAO = new UserDAO();
    }

    public StudySessionService(StudySessionDAO studySessionDAO, StudyRequestDAO studyRequestDAO, UserDAO userDAO) {
        this.studySessionDAO = studySessionDAO;
        this.studyRequestDAO = studyRequestDAO;
        this.userDAO = userDAO;
    }

    /**
     * Schedules a new peer study session from an accepted request.
     */
    public StudySession scheduleSession(int requestId, LocalDate date, LocalTime time, int currentUserId)
            throws ResourceNotFoundException, AuthorizationException, InvalidRequestException, ValidationException {

        ValidationUtil.validateFutureOrPresentDate(date);
        if (time == null) {
            throw new ValidationException("Session time is required.");
        }

        StudyRequest request = studyRequestDAO.findById(requestId);
        if (request == null) {
            throw new ResourceNotFoundException("Associated study request not found with ID: " + requestId);
        }

        // Rule: Only ACCEPTED requests can be scheduled
        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new InvalidRequestException(
                "A session can only be scheduled from an ACCEPTED study request. Current request status: " + request.getStatus()
            );
        }

        // Rule: Only one of the two participants may schedule
        if (request.getSenderId() != currentUserId && request.getReceiverId() != currentUserId) {
            throw new AuthorizationException("You are not a participant in this study request.");
        }

        // Rule: At most one active (SCHEDULED) session per request
        if (studySessionDAO.existsActiveForRequest(requestId)) {
            throw new InvalidRequestException("An active scheduled study session already exists for this study request.");
        }

        // Assemble participants
        User u1 = userDAO.findById(request.getSenderId());
        User u2 = userDAO.findById(request.getReceiverId());
        if (!(u1 instanceof Student s1) || !(u2 instanceof Student s2)) {
            throw new InvalidRequestException("Participants must be active registered students.");
        }

        StudySession session = new StudySession();
        session.setRequestId(requestId);
        session.setSubject(request.getSubject());
        session.setSessionDate(date);
        session.setSessionTime(time);
        session.setParticipants(new ArrayList<>(Arrays.asList(s1, s2)));

        session.schedule(); // Domain model invariant assertion

        List<Integer> participantIds = Arrays.asList(s1.getUserId(), s2.getUserId());
        return studySessionDAO.createSession(session, participantIds);
    }

    /**
     * Lists all sessions for a student participant.
     */
    public List<StudySession> listSessionsForUser(int userId) {
        return studySessionDAO.findByParticipant(userId);
    }

    /**
     * Retrieves session details with participation authorization check.
     */
    public StudySession getSessionDetails(int sessionId, int currentUserId)
            throws ResourceNotFoundException, AuthorizationException {

        StudySession session = studySessionDAO.findById(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("Study session not found with ID: " + sessionId);
        }

        User currentUser = userDAO.findById(currentUserId);
        boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (!isAdmin && !session.hasParticipant(currentUserId)) {
            throw new AuthorizationException("You are not authorized to view this study session.");
        }

        return session;
    }

    /**
     * Cancels an active session. Only permitted by one of the participants.
     */
    public void cancelSession(int sessionId, int currentUserId)
            throws ResourceNotFoundException, AuthorizationException, InvalidRequestException {

        StudySession session = getSessionDetails(sessionId, currentUserId);
        session.cancel(); // Asserts status is SCHEDULED
        studySessionDAO.updateStatus(sessionId, SessionStatus.CANCELLED);
    }

    /**
     * Marks a scheduled session as completed to permit peer review ratings.
     */
    public void markCompleted(int sessionId, int currentUserId)
            throws ResourceNotFoundException, AuthorizationException, InvalidRequestException {

        StudySession session = getSessionDetails(sessionId, currentUserId);
        session.complete(); // Asserts status is SCHEDULED
        studySessionDAO.updateStatus(sessionId, SessionStatus.COMPLETED);
    }
}
