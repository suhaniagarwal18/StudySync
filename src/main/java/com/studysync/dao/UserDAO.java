package com.studysync.dao;

import com.studysync.exception.DataAccessException;
import com.studysync.model.Admin;
import com.studysync.model.Student;
import com.studysync.model.User;
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
 * JDBC Data Access Object for User entities (Student and Admin).
 * 
 * CONCEPT: JDBC (Data Persistence, PreparedStatements, and Resource Management)
 * WHY: Encapsulates all SQL execution, protects against SQL injection through parameterized queries,
 * and uses try-with-resources to guarantee safe release of database connections and statements.
 */
public class UserDAO {

    private final SkillDAO skillDAO = new SkillDAO();

    /**
     * Persists a new user record (Student or Admin).
     */
    public User create(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, department, role, bio, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail().toLowerCase().trim());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getDepartment());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getBio());
            ps.setBoolean(7, user.isActive());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                    if (user instanceof Admin admin) {
                        admin.setAdminId(user.getUserId());
                    }
                } else {
                    throw new DataAccessException("Creating user failed, no ID obtained.");
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user into database: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a user by their unique primary key ID.
     */
    public User findById(int userId) {
        String sql = "SELECT user_id, name, email, password_hash, department, role, bio, is_active, created_at "
                   + "FROM users WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by ID: " + userId, e);
        }
    }

    /**
     * Finds a user by their unique email address.
     */
    public User findByEmail(String email) {
        if (email == null) {
            return null;
        }
        String sql = "SELECT user_id, name, email, password_hash, department, role, bio, is_active, created_at "
                   + "FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by email: " + email, e);
        }
    }

    /**
     * Checks if an email is already registered.
     */
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking email existence: " + email, e);
        }
    }

    /**
     * Loads a Student with skills (KNOWN and NEEDED) fully populated.
     */
    public Student loadStudentWithSkills(int userId) {
        User user = findById(userId);
        if (user instanceof Student student) {
            skillDAO.populateStudentSkills(student);
            return student;
        }
        return null;
    }

    /**
     * Updates profile details (name, department, bio).
     */
    public void updateProfile(int userId, String name, String department, String bio) {
        String sql = "UPDATE users SET name = ?, department = ?, bio = ? WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, department);
            ps.setString(3, bio);
            ps.setInt(4, userId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating profile for user ID: " + userId, e);
        }
    }

    /**
     * Toggles a user's active status.
     */
    public void setActive(int userId, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating active status for user ID: " + userId, e);
        }
    }

    /**
     * Retrieves all users (for admin management).
     */
    public List<User> listAll() {
        String sql = "SELECT user_id, name, email, password_hash, department, role, bio, is_active, created_at "
                   + "FROM users ORDER BY created_at DESC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error listing all users", e);
        }
    }

    /**
     * Helper to instantiate Student or Admin polymorphically from ResultSet.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;
        if ("ADMIN".equalsIgnoreCase(role)) {
            Admin admin = new Admin();
            admin.setAdminId(rs.getInt("user_id"));
            user = admin;
        } else {
            user = new Student();
        }

        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setDepartment(rs.getString("department"));
        user.setBio(rs.getString("bio"));
        user.setActive(rs.getBoolean("is_active"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }

        return user;
    }
}
