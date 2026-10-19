package com.studysync.dao;

import com.studysync.exception.DataAccessException;
import com.studysync.model.RequestStatus;
import com.studysync.model.Student;
import com.studysync.model.StudyRequest;
import com.studysync.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Data Access Object for StudyRequest entities.
 * 
 * CONCEPT: JDBC & Foreign Key Relationship Mapping
 * WHY: Joins study_requests with the users table to reconstruct hydrated Student
 * domain objects for senders and receivers.
 */
public class StudyRequestDAO {

    /**
     * Inserts a new study request.
     */
    public StudyRequest insert(StudyRequest request) {
        String sql = "INSERT INTO study_requests (sender_id, receiver_id, subject, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, request.getSenderId());
            ps.setInt(2, request.getReceiverId());
            ps.setString(3, request.getSubject());
            ps.setString(4, request.getStatus().name());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Creating study request failed, no rows affected.");
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    request.setRequestId(rs.getInt(1));
                }
            }
            return request;
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting study request: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a study request by ID, populating sender and receiver details.
     */
    public StudyRequest findById(int requestId) {
        String sql = "SELECT r.request_id, r.sender_id, r.receiver_id, r.subject, r.status, r.created_at, "
                   + "u1.name AS sender_name, u1.email AS sender_email, u1.department AS sender_dept, "
                   + "u2.name AS receiver_name, u2.email AS receiver_email, u2.department AS receiver_dept "
                   + "FROM study_requests r "
                   + "JOIN users u1 ON r.sender_id = u1.user_id "
                   + "JOIN users u2 ON r.receiver_id = u2.user_id "
                   + "WHERE r.request_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudyRequest(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding study request by ID: " + requestId, e);
        }
    }

    /**
     * Retrieves all study requests received by a student.
     */
    public List<StudyRequest> findReceived(int userId) {
        String sql = "SELECT r.request_id, r.sender_id, r.receiver_id, r.subject, r.status, r.created_at, "
                   + "u1.name AS sender_name, u1.email AS sender_email, u1.department AS sender_dept, "
                   + "u2.name AS receiver_name, u2.email AS receiver_email, u2.department AS receiver_dept "
                   + "FROM study_requests r "
                   + "JOIN users u1 ON r.sender_id = u1.user_id "
                   + "JOIN users u2 ON r.receiver_id = u2.user_id "
                   + "WHERE r.receiver_id = ? ORDER BY r.created_at DESC";
        return executeQueryList(sql, userId);
    }

    /**
     * Retrieves all study requests sent by a student.
     */
    public List<StudyRequest> findSent(int userId) {
        String sql = "SELECT r.request_id, r.sender_id, r.receiver_id, r.subject, r.status, r.created_at, "
                   + "u1.name AS sender_name, u1.email AS sender_email, u1.department AS sender_dept, "
                   + "u2.name AS receiver_name, u2.email AS receiver_email, u2.department AS receiver_dept "
                   + "FROM study_requests r "
                   + "JOIN users u1 ON r.sender_id = u1.user_id "
                   + "JOIN users u2 ON r.receiver_id = u2.user_id "
                   + "WHERE r.sender_id = ? ORDER BY r.created_at DESC";
        return executeQueryList(sql, userId);
    }

    /**
     * Checks if a pending request already exists between sender and receiver for the subject.
     */
    public boolean existsPendingBetween(int senderId, int receiverId, String subject) {
        String sql = "SELECT 1 FROM study_requests "
                   + "WHERE sender_id = ? AND receiver_id = ? AND LOWER(subject) = LOWER(?) AND status = 'PENDING'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, subject.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking pending request existence", e);
        }
    }

    /**
     * Updates the status of a study request (ACCEPTED or REJECTED).
     */
    public void updateStatus(int requestId, RequestStatus status) {
        String sql = "UPDATE study_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating status for request ID: " + requestId, e);
        }
    }

    private List<StudyRequest> executeQueryList(String sql, int paramId) {
        List<StudyRequest> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, paramId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToStudyRequest(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error executing study request list query", e);
        }
    }

    private StudyRequest mapRowToStudyRequest(ResultSet rs) throws SQLException {
        StudyRequest req = new StudyRequest();
        req.setRequestId(rs.getInt("request_id"));
        req.setSenderId(rs.getInt("sender_id"));
        req.setReceiverId(rs.getInt("receiver_id"));
        req.setSubject(rs.getString("subject"));
        req.setStatus(RequestStatus.valueOf(rs.getString("status")));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            req.setCreatedAt(ts.toLocalDateTime());
        }

        Student sender = new Student();
        sender.setUserId(req.getSenderId());
        sender.setName(rs.getString("sender_name"));
        sender.setEmail(rs.getString("sender_email"));
        sender.setDepartment(rs.getString("sender_dept"));
        req.setSender(sender);

        Student receiver = new Student();
        receiver.setUserId(req.getReceiverId());
        receiver.setName(rs.getString("receiver_name"));
        receiver.setEmail(rs.getString("receiver_email"));
        receiver.setDepartment(rs.getString("receiver_dept"));
        req.setReceiver(receiver);

        return req;
    }
}
