package com.studysync.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Encapsulates the recommendation result when matching a student with prospective study buddies.
 * 
 * CONCEPT: Comparable & Sorting (Collections.sort / Natural Ordering)
 * WHY: Implements Comparable to establish a natural ranking order:
 * Primary: matchScore descending
 * Secondary: averageRating descending
 * Tertiary: student name ascending (alphabetical)
 */
public class MatchResult implements Comparable<MatchResult> {

    private Student student;
    // CONCEPT: Collections (Set to hold unique intersected skills)
    private Set<String> commonSkills = new LinkedHashSet<>();
    private int matchScore;
    private double averageRating;
    private boolean mutualMatch;

    public MatchResult() {
    }

    public MatchResult(Student student, Set<String> commonSkills, int matchScore,
                       double averageRating, boolean mutualMatch) {
        this.student = student;
        this.commonSkills = commonSkills != null ? new LinkedHashSet<>(commonSkills) : new LinkedHashSet<>();
        this.matchScore = matchScore;
        this.averageRating = averageRating;
        this.mutualMatch = mutualMatch;
    }

    // --------------------------------------------------------------------------
    // Comparable Interface Implementation
    // --------------------------------------------------------------------------

    // CONCEPT: Comparable (Natural Ordering Comparison)
    @Override
    public int compareTo(MatchResult other) {
        if (other == null) {
            return -1;
        }
        // 1. Higher match score first
        int scoreCompare = Integer.compare(other.matchScore, this.matchScore);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        // 2. Higher average rating first
        int ratingCompare = Double.compare(other.averageRating, this.averageRating);
        if (ratingCompare != 0) {
            return ratingCompare;
        }
        // 3. Alphabetical by student name
        String name1 = this.student != null && this.student.getName() != null ? this.student.getName() : "";
        String name2 = other.student != null && other.student.getName() != null ? other.student.getName() : "";
        return name1.compareToIgnoreCase(name2);
    }

    // --------------------------------------------------------------------------
    // Getters and Setters
    // --------------------------------------------------------------------------

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Set<String> getCommonSkills() {
        return Collections.unmodifiableSet(commonSkills);
    }

    public void setCommonSkills(Set<String> commonSkills) {
        this.commonSkills = commonSkills != null ? new LinkedHashSet<>(commonSkills) : new LinkedHashSet<>();
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public boolean isMutualMatch() {
        return mutualMatch;
    }

    public void setMutualMatch(boolean mutualMatch) {
        this.mutualMatch = mutualMatch;
    }

    public String getFormattedRating() {
        return String.format(java.util.Locale.US, "%.1f", averageRating);
    }
}
