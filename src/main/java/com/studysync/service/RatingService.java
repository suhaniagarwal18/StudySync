package com.studysync.service;

import com.studysync.dao.RatingDAO;
import com.studysync.dao.StudySessionDAO;
import com.studysync.exception.AuthorizationException;
import com.studysync.exception.InvalidRequestException;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.exception.ValidationException;
import com.studysync.model.Rating;
import com.studysync.model.SessionStatus;
import com.studysync.model.Student;
import com.studysync.model.StudySession;
import com.studysync.util.ValidationUtil;
import java.util.List;

/**
 * Service orchestrating peer evaluation ratings and feedback submission.
 * 
 * CONCEPT: Business Rules & Data Integrity
 * WHY: Enforces that peer reviews can only be submitted for completed study sessions,
 * by an authentic participant targeting the other session buddy, without duplicate evaluations.
 */
public class RatingService {

    private final RatingDAO ratingDAO;
    private final StudySessionDAO studySessionDAO;

    public RatingService() {
        this.ratingDAO = new RatingDAO();
        this.studySessionDAO = new StudySessionDAO();
    }

    public RatingService(RatingDAO ratingDAO, StudySessionDAO studySessionDAO) {
        this.ratingDAO = ratingDAO;
        this.studySessionDAO = studySessionDAO;
    }

    /**
     * Submits a peer rating and review for a completed study session.
     */
    public Rating submitRating(int sessionId, int reviewerId, int ratingValue, String feedback)
            throws ResourceNotFoundException, AuthorizationException, InvalidRequestException, ValidationException {

        ValidationUtil.validateRating(ratingValue, feedback);

        StudySession session = studySessionDAO.findById(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("Study session not found with ID: " + sessionId);
        }

        // Rule: Only COMPLETED sessions can be reviewed
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new InvalidRequestException(
                "Feedback ratings can only be submitted after a study session is COMPLETED. Current session status: "
              + session.getStatus()
            );
        }

        // Rule: Reviewer must be a participant in the session
        if (!session.hasParticipant(reviewerId)) {
            throw new AuthorizationException("You cannot submit a review for a session you did not participate in.");
        }

        // Rule: Target reviewee is the OTHER participant
        Student otherParticipant = session.getOtherParticipant(reviewerId);
        if (otherParticipant == null) {
            throw new InvalidRequestException("Could not determine peer participant for this study session.");
        }
        int revieweeId = otherParticipant.getUserId();

        // Rule: Cannot rate oneself
        if (reviewerId == revieweeId) {
            throw new InvalidRequestException("You cannot submit a peer rating for yourself.");
        }

        // Rule: No duplicate ratings for the same session from the same reviewer
        if (ratingDAO.existsByReviewerSession(sessionId, reviewerId)) {
            throw new InvalidRequestException("You have already submitted a feedback rating for this study session.");
        }

        Rating rating = new Rating(sessionId, reviewerId, revieweeId, ratingValue, feedback);
        return ratingDAO.insert(rating);
    }

    public List<Rating> getRatingsForStudent(int studentId) {
        return ratingDAO.findByRevieweeId(studentId);
    }

    public double getAverageRating(int studentId) {
        return ratingDAO.getAverageRatingForUser(studentId);
    }

    public boolean hasRated(int sessionId, int userId) {
        return ratingDAO.existsByReviewerSession(sessionId, userId);
    }
}
