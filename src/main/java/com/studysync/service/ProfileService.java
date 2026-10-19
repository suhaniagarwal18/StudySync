package com.studysync.service;

import com.studysync.dao.SkillDAO;
import com.studysync.dao.UserDAO;
import com.studysync.exception.ResourceNotFoundException;
import com.studysync.exception.ValidationException;
import com.studysync.model.SkillType;
import com.studysync.model.Student;
import com.studysync.model.User;
import com.studysync.util.ValidationUtil;
import java.util.List;
import java.util.Set;

/**
 * Service managing student profile information, bios, and skill associations.
 * 
 * CONCEPT: Business Invariant Enforcement
 * WHY: Enforces cross-collection constraints, such as ensuring a skill cannot simultaneously
 * exist in both a student's KNOWN and NEEDED lists.
 */
public class ProfileService {

    private final UserDAO userDAO;
    private final SkillDAO skillDAO;

    public ProfileService() {
        this.userDAO = new UserDAO();
        this.skillDAO = new SkillDAO();
    }

    public ProfileService(UserDAO userDAO, SkillDAO skillDAO) {
        this.userDAO = userDAO;
        this.skillDAO = skillDAO;
    }

    /**
     * Loads a user by ID with populated skills if student.
     */
    public User getUserProfile(int userId) throws ResourceNotFoundException {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        if (user instanceof Student student) {
            skillDAO.populateStudentSkills(student);
            return student;
        }
        return user;
    }

    /**
     * Updates profile details (name, department, bio).
     */
    public void updateProfile(int userId, String name, String department, String bio)
            throws ValidationException, ResourceNotFoundException {
        ValidationUtil.validateProfileUpdate(name, department, bio);
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        userDAO.updateProfile(userId, name.trim(), department != null ? department.trim() : null, bio != null ? bio.trim() : null);
    }

    /**
     * Adds a skill to a student's profile after preventing duplicate or contradictory entries.
     */
    public void addSkill(int userId, String skillName, SkillType type)
            throws ValidationException, ResourceNotFoundException {
        ValidationUtil.validateSkillName(skillName);
        String normalized = ValidationUtil.normalizeSkillName(skillName);
        String comparisonKey = ValidationUtil.toComparisonKey(normalized);

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        // Cross-category invariant check: cannot be both KNOWN and NEEDED
        SkillType oppositeType = (type == SkillType.KNOWN) ? SkillType.NEEDED : SkillType.KNOWN;
        Set<String> oppositeSkills = skillDAO.getSkills(userId, oppositeType);
        for (String opp : oppositeSkills) {
            if (ValidationUtil.toComparisonKey(opp).equals(comparisonKey)) {
                String oppName = (type == SkillType.KNOWN) ? "needed" : "known";
                throw new ValidationException(
                    "Cannot add '" + normalized + "' as " + type.name().toLowerCase()
                  + " because it is currently listed under your " + oppName + " skills."
                );
            }
        }

        skillDAO.addSkillToUser(userId, normalized, type);
    }

    /**
     * Removes a skill association from a student's profile.
     */
    public void removeSkill(int userId, String skillName, SkillType type) {
        if (skillName != null && !skillName.isBlank()) {
            skillDAO.removeSkillFromUser(userId, skillName.trim(), type);
        }
    }

    /**
     * Returns all registered skills across the platform for auto-suggestions.
     */
    public List<String> getAllSkills() {
        return skillDAO.getAllSkillNames();
    }
}
