package com.studysync.service;

import com.studysync.dao.SkillDAO;
import com.studysync.dao.StudyRequestDAO;
import com.studysync.dao.UserDAO;
import com.studysync.exception.AuthorizationException;
import com.studysync.exception.InvalidRequestException;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.exception.ValidationException;
import com.studysync.model.RequestStatus;
import com.studysync.model.SkillType;
import com.studysync.model.Student;
import com.studysync.model.StudyRequest;
import com.studysync.model.User;
import com.studysync.util.ValidationUtil;
import java.util.List;
import java.util.Set;

/**
 * Service managing peer study requests and state lifecycle.
 * 
 * CONCEPT: Authorization & Business Invariant Enforcement
 * WHY: Protects requests from unauthorized mutation (e.g., verifying that only the targeted
 * receiver can accept or reject a request) and prevents duplicate pending invitations.
 */
public class StudyRequestService {

    private final StudyRequestDAO studyRequestDAO;
    private final UserDAO userDAO;
    private final SkillDAO skillDAO;

    public StudyRequestService() {
        this.studyRequestDAO = new StudyRequestDAO();
        this.userDAO = new UserDAO();
        this.skillDAO = new SkillDAO();
    }

    public StudyRequestService(StudyRequestDAO studyRequestDAO, UserDAO userDAO, SkillDAO skillDAO) {
        this.studyRequestDAO = studyRequestDAO;
        this.userDAO = userDAO;
        this.skillDAO = skillDAO;
    }

    /**
     * Creates and sends a new study request to another student.
     */
    public StudyRequest sendRequest(int senderId, int receiverId, String subject)
            throws ValidationException, InvalidRequestException, ResourceNotFoundException {

        ValidationUtil.validateSubject(subject);
        String normalizedSubject = ValidationUtil.normalizeSkillName(subject);

        if (senderId == receiverId) {
            throw new InvalidRequestException("You cannot send a study buddy request to yourself.");
        }

        User receiver = userDAO.findById(receiverId);
        if (receiver == null || !(receiver instanceof Student) || !receiver.isActive()) {
            throw new ResourceNotFoundException("Target student does not exist or is inactive.");
        }

        // Verify receiver actually knows this subject
        Set<String> receiverKnown = skillDAO.getSkills(receiverId, SkillType.KNOWN);
        String subjectKey = ValidationUtil.toComparisonKey(normalizedSubject);
        boolean knowsSubject = receiverKnown.stream().anyMatch(s -> ValidationUtil.toComparisonKey(s).equals(subjectKey));

        if (!knowsSubject) {
            throw new InvalidRequestException(
                "Student '" + receiver.getName() + "' has not listed '" + normalizedSubject + "' under their known skills."
            );
        }

        // Check for duplicate pending requests
        if (studyRequestDAO.existsPendingBetween(senderId, receiverId, normalizedSubject)) {
            throw new InvalidRequestException(
                "A pending study request for '" + normalizedSubject + "' has already been sent to this student."
            );
        }

        StudyRequest request = new StudyRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setSubject(normalizedSubject);
        request.setStatus(RequestStatus.PENDING);

        return studyRequestDAO.insert(request);
    }

    /**
     * Accepts a pending study request. Only the receiver is authorized to accept.
     */
    public void acceptRequest(int requestId, int currentUserId)
            throws ResourceNotFoundException, AuthorizationException, InvalidRequestException {

        StudyRequest req = studyRequestDAO.findById(requestId);
        if (req == null) {
            throw new ResourceNotFoundException("Study request not found with ID: " + requestId);
        }

        if (req.getReceiverId() != currentUserId) {
            throw new AuthorizationException("Only the recipient of this study request is authorized to accept it.");
        }

        req.acceptRequest(); // Domain model invariant check
        studyRequestDAO.updateStatus(requestId, RequestStatus.ACCEPTED);
    }

    /**
     * Rejects a pending study request. Only the receiver is authorized to reject.
     */
    public void rejectRequest(int requestId, int currentUserId)
            throws ResourceNotFoundException, AuthorizationException, InvalidRequestException {

        StudyRequest req = studyRequestDAO.findById(requestId);
        if (req == null) {
            throw new ResourceNotFoundException("Study request not found with ID: " + requestId);
        }

        if (req.getReceiverId() != currentUserId) {
            throw new AuthorizationException("Only the recipient of this study request is authorized to reject it.");
        }

        req.rejectRequest(); // Domain model invariant check
        studyRequestDAO.updateStatus(requestId, RequestStatus.REJECTED);
    }

    public List<StudyRequest> listReceivedRequests(int userId) {
        return studyRequestDAO.findReceived(userId);
    }

    public List<StudyRequest> listSentRequests(int userId) {
        return studyRequestDAO.findSent(userId);
    }

    public StudyRequest getRequestById(int requestId) throws ResourceNotFoundException {
        StudyRequest req = studyRequestDAO.findById(requestId);
        if (req == null) {
            throw new ResourceNotFoundException("Study request not found with ID: " + requestId);
        }
        return req;
    }
}
