package com.studysync.dao;

import com.studysync.exception.DataAccessException;
import com.studysync.model.SessionStatus;
import com.studysync.model.Student;
import com.studysync.model.StudySession;
import com.studysync.util.DBUtil;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Data Access Object for StudySession and SessionParticipants entities.
 * 
 * CONCEPT: JDBC Transactions (ACID Properties)
 * WHY: Multi-table inserts (creating a study session row and associated participant rows)
 * must be strictly atomic. Uses setAutoCommit(false), commit(), and rollback() so that
 * failures never leave partial or orphaned records.
 */
public class StudySessionDAO {

    /**
     * Atomically creates a study session and enrolls participants in a single database transaction.
     */
    public StudySession createSession(StudySession session, List<Integer> participantIds) {
        String sessionSql = "INSERT INTO study_sessions (request_id, subject, session_date, session_time, status) "
                          + "VALUES (?, ?, ?, ?, ?)";
        String participantSql = "INSERT INTO session_participants (session_id, student_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            DBUtil.beginTransaction(conn); // conn.setAutoCommit(false)

            int sessionId;
            try (PreparedStatement psSession = conn.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS)) {
                psSession.setInt(1, session.getRequestId());
                psSession.setString(2, session.getSubject());
                psSession.setDate(3, Date.valueOf(session.getSessionDate()));
                psSession.setTime(4, Time.valueOf(session.getSessionTime()));
                psSession.setString(5, session.getStatus().name());

                int affected = psSession.executeUpdate();
                if (affected == 0) {
                    throw new DataAccessException("Creating session failed, no rows affected.");
                }

                try (ResultSet rs = psSession.getGeneratedKeys()) {
                    if (rs.next()) {
                        sessionId = rs.getInt(1);
                        session.setSessionId(sessionId);
                    } else {
                        throw new DataAccessException("Creating session failed, no ID obtained.");
                    }
                }
            }

            // Insert participants
            try (PreparedStatement psPart = conn.prepareStatement(participantSql)) {
                for (int studentId : participantIds) {
                    psPart.setInt(1, sessionId);
                    psPart.setInt(2, studentId);
                    psPart.addBatch();
                }
                psPart.executeBatch();
            }

            DBUtil.commitTransaction(conn); // conn.commit()
            return session;
        } catch (SQLException e) {
            DBUtil.rollbackTransaction(conn); // conn.rollback()
            throw new DataAccessException("Transaction failed while creating study session: " + e.getMessage(), e);
        } finally {
            DBUtil.close(conn);
        }
    }

    /**
     * Finds a session by its primary key ID, hydrating all participants.
     */
    public StudySession findById(int sessionId) {
        String sessionSql = "SELECT session_id, request_id, subject, session_date, session_time, status "
                          + "FROM study_sessions WHERE session_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sessionSql)) {

            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudySession session = mapRowToSession(rs);
                    session.setParticipants(loadParticipants(conn, sessionId));
                    return session;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding study session by ID: " + sessionId, e);
        }
    }

    /**
     * Lists all study sessions involving a particular student as participant.
     */
    public List<StudySession> findByParticipant(int userId) {
        String sql = "SELECT s.session_id, s.request_id, s.subject, s.session_date, s.session_time, s.status "
                   + "FROM study_sessions s "
                   + "JOIN session_participants sp ON s.session_id = sp.session_id "
                   + "WHERE sp.student_id = ? "
                   + "ORDER BY s.session_date DESC, s.session_time DESC";
        List<StudySession> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudySession s = mapRowToSession(rs);
                    s.setParticipants(loadParticipants(conn, s.getSessionId()));
                    list.add(s);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding sessions for participant ID: " + userId, e);
        }
    }

    /**
     * Updates session status (COMPLETED, CANCELLED).
     */
    public void updateStatus(int sessionId, SessionStatus status) {
        String sql = "UPDATE study_sessions SET status = ? WHERE session_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, sessionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating status for session ID: " + sessionId, e);
        }
    }

    /**
     * Verifies if an active SCHEDULED session already exists for an accepted study request.
     */
    public boolean existsActiveForRequest(int requestId) {
        String sql = "SELECT 1 FROM study_sessions WHERE request_id = ? AND status = 'SCHEDULED'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking active session for request ID: " + requestId, e);
        }
    }

    private List<Student> loadParticipants(Connection conn, int sessionId) throws SQLException {
        String sql = "SELECT u.user_id, u.name, u.email, u.department "
                   + "FROM session_participants sp "
                   + "JOIN users u ON sp.student_id = u.user_id "
                   + "WHERE sp.session_id = ?";
        List<Student> students = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student st = new Student();
                    st.setUserId(rs.getInt("user_id"));
                    st.setName(rs.getString("name"));
                    st.setEmail(rs.getString("email"));
                    st.setDepartment(rs.getString("department"));
                    students.add(st);
                }
            }
        }
        return students;
    }

    private StudySession mapRowToSession(ResultSet rs) throws SQLException {
        StudySession s = new StudySession();
        s.setSessionId(rs.getInt("session_id"));
        s.setRequestId(rs.getInt("request_id"));
        s.setSubject(rs.getString("subject"));

        Date d = rs.getDate("session_date");
        if (d != null) {
            s.setSessionDate(d.toLocalDate());
        }

        Time t = rs.getTime("session_time");
        if (t != null) {
            s.setSessionTime(t.toLocalTime());
        }

        s.setStatus(SessionStatus.valueOf(rs.getString("status")));
        return s;
    }
}
