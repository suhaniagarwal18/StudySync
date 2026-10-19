package com.studysync.dao;

import com.studysync.exception.DataAccessException;
import com.studysync.model.Rating;
import com.studysync.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC Data Access Object for peer Rating and feedback entities.
 * 
 * CONCEPT: JDBC & SQL Aggregate Functions (AVG, COUNT)
 * WHY: Calculates average feedback scores and loads historical reviews for student profiles
 * and recommendation matching scores.
 */
public class RatingDAO {

    /**
     * Inserts a new peer rating record.
     */
    public Rating insert(Rating rating) {
        String sql = "INSERT INTO ratings (session_id, reviewer_id, reviewee_id, rating_value, feedback) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, rating.getSessionId());
            ps.setInt(2, rating.getReviewerId());
            ps.setInt(3, rating.getRevieweeId());
            ps.setInt(4, rating.getRatingValue());
            ps.setString(5, rating.getFeedback());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Inserting rating failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    rating.setRatingId(rs.getInt(1));
                }
            }
            return rating;
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting rating: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all feedback ratings received by a student.
     */
    public List<Rating> findByRevieweeId(int revieweeId) {
        String sql = "SELECT r.rating_id, r.session_id, r.reviewer_id, r.reviewee_id, r.rating_value, r.feedback, r.created_at, "
                   + "u1.name AS reviewer_name, u2.name AS reviewee_name, s.subject AS session_subject "
                   + "FROM ratings r "
                   + "JOIN users u1 ON r.reviewer_id = u1.user_id "
                   + "JOIN users u2 ON r.reviewee_id = u2.user_id "
                   + "JOIN study_sessions s ON r.session_id = s.session_id "
                   + "WHERE r.reviewee_id = ? ORDER BY r.created_at DESC";
        List<Rating> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, revieweeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRating(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving ratings for reviewee ID: " + revieweeId, e);
        }
    }

    /**
     * Retrieves ratings given within a specific session.
     */
    public List<Rating> findBySession(int sessionId) {
        String sql = "SELECT r.rating_id, r.session_id, r.reviewer_id, r.reviewee_id, r.rating_value, r.feedback, r.created_at, "
                   + "u1.name AS reviewer_name, u2.name AS reviewee_name, s.subject AS session_subject "
                   + "FROM ratings r "
                   + "JOIN users u1 ON r.reviewer_id = u1.user_id "
                   + "JOIN users u2 ON r.reviewee_id = u2.user_id "
                   + "JOIN study_sessions s ON r.session_id = s.session_id "
                   + "WHERE r.session_id = ? ORDER BY r.created_at ASC";
        List<Rating> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRating(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving ratings for session ID: " + sessionId, e);
        }
    }

    /**
     * Checks if a reviewer has already submitted a rating for a specific session.
     */
    public boolean existsByReviewerSession(int sessionId, int reviewerId) {
        String sql = "SELECT 1 FROM ratings WHERE session_id = ? AND reviewer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            ps.setInt(2, reviewerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking existing rating for session and reviewer", e);
        }
    }

    /**
     * Calculates the average star rating for a student (0.0 if unrated).
     */
    public double getAverageRatingForUser(int userId) {
        String sql = "SELECT COALESCE(AVG(rating_value), 0.0) AS avg_rating FROM ratings WHERE reviewee_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
            return 0.0;
        } catch (SQLException e) {
            throw new DataAccessException("Error calculating average rating for user ID: " + userId, e);
        }
    }

    /**
     * Bulk loads average ratings for all active students.
     * 
     * CONCEPT: Collections (HashMap for O(1) rating lookup during buddy recommendation)
     */
    public Map<Integer, Double> getAverageRatingsMap() {
        String sql = "SELECT reviewee_id, AVG(rating_value) AS avg_rating FROM ratings GROUP BY reviewee_id";
        Map<Integer, Double> map = new HashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getInt("reviewee_id"), rs.getDouble("avg_rating"));
            }
            return map;
        } catch (SQLException e) {
            throw new DataAccessException("Error bulk-loading average ratings map", e);
        }
    }

    private Rating mapRowToRating(ResultSet rs) throws SQLException {
        Rating r = new Rating();
        r.setRatingId(rs.getInt("rating_id"));
        r.setSessionId(rs.getInt("session_id"));
        r.setReviewerId(rs.getInt("reviewer_id"));
        r.setRevieweeId(rs.getInt("reviewee_id"));
        r.setReviewerName(rs.getString("reviewer_name"));
        r.setRevieweeName(rs.getString("reviewee_name"));
        r.setSessionSubject(rs.getString("session_subject"));
        try {
            r.setRatingValue(rs.getInt("rating_value"));
            r.setFeedback(rs.getString("feedback"));
        } catch (Exception e) {
            // Internal mapping fallback
        }

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            r.setCreatedAt(ts.toLocalDateTime());
        }
        return r;
    }
}
