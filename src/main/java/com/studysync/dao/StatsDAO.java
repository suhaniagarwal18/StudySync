package com.studysync.dao;

import com.studysync.exception.DataAccessException;
import com.studysync.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JDBC Data Access Object for platform-wide metrics and analytics.
 * 
 * CONCEPT: JDBC & SQL Aggregations (COUNT, GROUP BY, AVG)
 * WHY: Powers the Admin Dashboard with aggregated statistical reporting.
 */
public class StatsDAO {

    public int getTotalUsersCount() {
        return getCount("SELECT COUNT(*) FROM users");
    }

    public int getStudentCount() {
        return getCount("SELECT COUNT(*) FROM users WHERE role = 'STUDENT'");
    }

    public int getActiveStudentCount() {
        return getCount("SELECT COUNT(*) FROM users WHERE role = 'STUDENT' AND is_active = TRUE");
    }

    public int getTotalNotesCount() {
        return getCount("SELECT COUNT(*) FROM notes");
    }

    public int getTotalRequestsCount() {
        return getCount("SELECT COUNT(*) FROM study_requests");
    }

    public Map<String, Integer> getRequestsCountByStatus() {
        return getCountMap("SELECT status, COUNT(*) AS cnt FROM study_requests GROUP BY status");
    }

    public int getTotalSessionsCount() {
        return getCount("SELECT COUNT(*) FROM study_sessions");
    }

    public Map<String, Integer> getSessionsCountByStatus() {
        return getCountMap("SELECT status, COUNT(*) AS cnt FROM study_sessions GROUP BY status");
    }

    public double getOverallAverageRating() {
        String sql = "SELECT COALESCE(AVG(rating_value), 0.0) AS avg_rating FROM ratings";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
            return 0.0;
        } catch (SQLException e) {
            throw new DataAccessException("Error calculating platform overall average rating", e);
        }
    }

    /**
     * Retrieves top subjects by notes count.
     */
    public Map<String, Integer> getTopSubjects() {
        String sql = "SELECT subject, COUNT(*) AS cnt FROM notes GROUP BY subject ORDER BY cnt DESC LIMIT 5";
        return getCountMap(sql);
    }

    private int getCount(String sql) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error executing count query: " + sql, e);
        }
    }

    private Map<String, Integer> getCountMap(String sql) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
            return map;
        } catch (SQLException e) {
            throw new DataAccessException("Error executing aggregate map query: " + sql, e);
        }
    }
}
