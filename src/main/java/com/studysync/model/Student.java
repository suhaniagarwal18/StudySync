package com.studysync.model;

import com.studysync.exception.ValidationException;
import com.studysync.util.ValidationUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Concrete domain model representing a student participant in the StudySync network.
 * 
 * CONCEPT: Inheritance (Specialization)
 * WHY: Inherits identity and profile fields from User while adding student-specific
 * capabilities: managing known skills, needed skills, and searching for study buddies.
 */
public class Student extends User {

    // CONCEPT: Collections (LinkedHashSet preserves insertion order while guaranteeing unique skills)
    private Set<String> skillsKnown = new LinkedHashSet<>();
    private Set<String> skillsNeeded = new LinkedHashSet<>();

    public Student() {
        super();
    }

    public Student(int userId, String name, String email, String passwordHash, String department) {
        super(userId, name, email, passwordHash, department);
    }

    /**
     * Adds a skill to either KNOWN or NEEDED list after normalization and cross-list validation.
     */
    public void addSkill(String skillName, SkillType type) throws ValidationException {
        ValidationUtil.validateSkillName(skillName);
        String normalized = ValidationUtil.normalizeSkillName(skillName);
        String key = ValidationUtil.toComparisonKey(normalized);

        if (type == SkillType.KNOWN) {
            // Cannot need what you already know
            for (String needed : skillsNeeded) {
                if (ValidationUtil.toComparisonKey(needed).equals(key)) {
                    throw new ValidationException("Cannot add '" + normalized + "' as a known skill while it is listed as needed. Remove it from needed skills first.");
                }
            }
            skillsKnown.add(normalized);
        } else {
            // Cannot learn what you already know
            for (String known : skillsKnown) {
                if (ValidationUtil.toComparisonKey(known).equals(key)) {
                    throw new ValidationException("Cannot add '" + normalized + "' as needed while it is listed as a known skill. Remove it from known skills first.");
                }
            }
            skillsNeeded.add(normalized);
        }
    }

    /**
     * Removes a skill from the designated set.
     */
    public void removeSkill(String skillName, SkillType type) {
        if (skillName == null) {
            return;
        }
        String key = ValidationUtil.toComparisonKey(skillName);
        if (type == SkillType.KNOWN) {
            skillsKnown.removeIf(s -> ValidationUtil.toComparisonKey(s).equals(key));
        } else {
            skillsNeeded.removeIf(s -> ValidationUtil.toComparisonKey(s).equals(key));
        }
    }

    // --------------------------------------------------------------------------
    // Polymorphic Overrides
    // --------------------------------------------------------------------------

    // CONCEPT: Polymorphism (Method Overriding)
    @Override
    public String getRole() {
        return "STUDENT";
    }

    // CONCEPT: Polymorphism (Role-specific navigation entry)
    @Override
    public String getDashboardPath() {
        return "/dashboard";
    }

    // CONCEPT: Polymorphism (Dynamic role-based navbar generation)
    @Override
    public List<String> getMenuItems() {
        // CONCEPT: Collections (ArrayList for ordered display list)
        return new ArrayList<>(Arrays.asList(
            "Dashboard",
            "Find Buddies",
            "Study Requests",
            "Study Sessions",
            "Notes Exchange",
            "Ratings",
            "My Profile"
        ));
    }

    // --------------------------------------------------------------------------
    // Getters and Setters
    // --------------------------------------------------------------------------

    public Set<String> getSkillsKnown() {
        return Collections.unmodifiableSet(skillsKnown);
    }

    public void setSkillsKnown(Set<String> skillsKnown) {
        this.skillsKnown = skillsKnown != null ? new LinkedHashSet<>(skillsKnown) : new LinkedHashSet<>();
    }

    public Set<String> getSkillsNeeded() {
        return Collections.unmodifiableSet(skillsNeeded);
    }

    public void setSkillsNeeded(Set<String> skillsNeeded) {
        this.skillsNeeded = skillsNeeded != null ? new LinkedHashSet<>(skillsNeeded) : new LinkedHashSet<>();
    }
}
