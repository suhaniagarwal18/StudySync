package com.studysync.dao;

import com.studysync.exception.DataAccessException;
import com.studysync.model.SkillType;
import com.studysync.model.Student;
import com.studysync.util.DBUtil;
import com.studysync.util.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JDBC Data Access Object for Skill dictionary and Student-Skill associations.
 * 
 * CONCEPT: JDBC & Normalized Schema Access
 * WHY: Maintains a normalized skills dictionary while efficiently querying student-skill
 * relationships using indexed joins.
 */
public class SkillDAO {

    /**
     * Finds a skill ID by canonical name, or creates a new entry if absent.
     */
    public int findOrCreateSkill(String rawName) {
        String normalized = ValidationUtil.normalizeSkillName(rawName);
        String selectSql = "SELECT skill_id FROM skills WHERE LOWER(skill_name) = LOWER(?)";
        String insertSql = "INSERT INTO skills (skill_name) VALUES (?)";

        try (Connection conn = DBUtil.getConnection()) {
            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setString(1, normalized);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("skill_id");
                    }
                }
            }

            // Create new skill entry
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setString(1, normalized);
                psInsert.executeUpdate();
                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            throw new DataAccessException("Failed to obtain ID for skill: " + normalized);
        } catch (SQLException e) {
            // Check if concurrent insert occurred
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setString(1, normalized);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("skill_id");
                    }
                }
            } catch (SQLException ex) {
                // Ignore fallback error
            }
            throw new DataAccessException("Error finding or creating skill: " + rawName, e);
        }
    }

    /**
     * Associates a skill with a student as either KNOWN or NEEDED.
     */
    public void addSkillToUser(int userId, String skillName, SkillType type) {
        int skillId = findOrCreateSkill(skillName);
        String sql = "INSERT IGNORE INTO student_skills (user_id, skill_id, skill_type) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, skillId);
            ps.setString(3, type.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error associating skill '" + skillName + "' to user: " + userId, e);
        }
    }

    /**
     * Removes an association between a student and a skill.
     */
    public void removeSkillFromUser(int userId, String skillName, SkillType type) {
        String sql = "DELETE ss FROM student_skills ss "
                   + "JOIN skills s ON ss.skill_id = s.skill_id "
                   + "WHERE ss.user_id = ? AND LOWER(s.skill_name) = LOWER(?) AND ss.skill_type = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, skillName.trim());
            ps.setString(3, type.name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error removing skill '" + skillName + "' from user: " + userId, e);
        }
    }

    /**
     * Retrieves all skills associated with a user for a given type.
     */
    public Set<String> getSkills(int userId, SkillType type) {
        String sql = "SELECT s.skill_name FROM student_skills ss "
                   + "JOIN skills s ON ss.skill_id = s.skill_id "
                   + "WHERE ss.user_id = ? AND ss.skill_type = ? "
                   + "ORDER BY s.skill_name ASC";
        // CONCEPT: Collections (LinkedHashSet preserves alphabetical sorting while ensuring uniqueness)
        Set<String> set = new LinkedHashSet<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getString("skill_name"));
                }
            }
            return set;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving skills for user ID: " + userId, e);
        }
    }

    /**
     * Populates both KNOWN and NEEDED skill sets on a Student model object.
     */
    public void populateStudentSkills(Student student) {
        if (student == null) {
            return;
        }
        student.setSkillsKnown(getSkills(student.getUserId(), SkillType.KNOWN));
        student.setSkillsNeeded(getSkills(student.getUserId(), SkillType.NEEDED));
    }

    /**
     * Retrieves all unique registered skill names in the system.
     */
    public List<String> getAllSkillNames() {
        String sql = "SELECT skill_name FROM skills ORDER BY skill_name ASC";
        List<String> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("skill_name"));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving all skill names", e);
        }
    }

    /**
     * Bulk loads known skills for all active students.
     * 
     * CONCEPT: Collections (Map<Integer, Set<String>> using HashMap for O(1) matching access)
     * WHY: Prevents N+1 database queries during buddy matching. Loads all candidate skills
     * in a single query mapped by student ID.
     */
    public Map<Integer, Set<String>> getKnownSkillsByAllStudents() {
        return getSkillsMapByType(SkillType.KNOWN);
    }

    /**
     * Bulk loads needed skills for all active students (used to compute mutual matches).
     */
    public Map<Integer, Set<String>> getNeededSkillsByAllStudents() {
        return getSkillsMapByType(SkillType.NEEDED);
    }

    private Map<Integer, Set<String>> getSkillsMapByType(SkillType type) {
        String sql = "SELECT ss.user_id, s.skill_name "
                   + "FROM student_skills ss "
                   + "JOIN skills s ON ss.skill_id = s.skill_id "
                   + "JOIN users u ON ss.user_id = u.user_id "
                   + "WHERE ss.skill_type = ? AND u.is_active = TRUE AND u.role = 'STUDENT'";
        // CONCEPT: Collections (HashMap for fast O(1) user key lookup)
        Map<Integer, Set<String>> map = new HashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String skill = rs.getString("skill_name");
                    map.computeIfAbsent(userId, k -> new LinkedHashSet<>()).add(skill);
                }
            }
            return map;
        } catch (SQLException e) {
            throw new DataAccessException("Error bulk-loading student skills for type: " + type, e);
        }
    }
}
