package com.studysync.service;

import com.studysync.dao.RatingDAO;
import com.studysync.dao.SkillDAO;
import com.studysync.dao.UserDAO;
import com.studysync.model.MatchResult;
import com.studysync.model.Searchable;
import com.studysync.model.SkillType;
import com.studysync.model.Student;
import com.studysync.model.User;
import com.studysync.util.ValidationUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service executing peer study buddy matching and subject-based student discovery.
 * 
 * CONCEPT: Algorithms & Collections (Set Intersection, Hash-Lookup, and Comparator Sorting)
 * WHY: Demonstrates core computer science concepts:
 * - HashSet: O(1) membership test and mathematical set intersection (retainAll).
 * - HashMap: O(1) key-based aggregation to eliminate N+1 database queries.
 * - ArrayList: contiguous memory storage for ordered ranking results.
 * - Comparator: custom multi-property ranking without machine learning complexity.
 */
public class MatchingService implements Searchable<MatchResult> {

    private final UserDAO userDAO;
    private final SkillDAO skillDAO;
    private final RatingDAO ratingDAO;

    public MatchingService() {
        this.userDAO = new UserDAO();
        this.skillDAO = new SkillDAO();
        this.ratingDAO = new RatingDAO();
    }

    public MatchingService(UserDAO userDAO, SkillDAO skillDAO, RatingDAO ratingDAO) {
        this.userDAO = userDAO;
        this.skillDAO = skillDAO;
        this.ratingDAO = ratingDAO;
    }

    /**
     * Implements Searchable interface by delegating criteria search to subject lookup.
     */
    @Override
    public List<MatchResult> search(String subjectCriteria) {
        return searchStudentsBySubject(0, subjectCriteria);
    }

    /**
     * Computes peer study buddy recommendations for a given student based on skill needs.
     * 
     * @param requester the student looking for study buddies
     * @return sorted list of MatchResult objects
     */
    public List<MatchResult> findMatchesForStudent(Student requester) {
        if (requester == null) {
            return Collections.emptyList();
        }

        // STEP 1: Load requester's needed skills into a HashSet for O(1) lookup
        // CONCEPT: Collections (HashSet guarantees unique elements and fast contains/intersection)
        Set<String> neededNormalized = new HashSet<>();
        for (String needed : requester.getSkillsNeeded()) {
            neededNormalized.add(ValidationUtil.toComparisonKey(needed));
        }

        if (neededNormalized.isEmpty()) {
            return Collections.emptyList();
        }

        // Requester's known skills (for detecting mutual matches)
        Set<String> requesterKnownNormalized = new HashSet<>();
        for (String known : requester.getSkillsKnown()) {
            requesterKnownNormalized.add(ValidationUtil.toComparisonKey(known));
        }

        // STEP 2: Bulk load all active students' known & needed skills in one query
        // CONCEPT: Collections (HashMap provides O(1) student ID to skills set lookup)
        Map<Integer, Set<String>> allKnownMap = skillDAO.getKnownSkillsByAllStudents();
        Map<Integer, Set<String>> allNeededMap = skillDAO.getNeededSkillsByAllStudents();
        Map<Integer, Double> avgRatingsMap = ratingDAO.getAverageRatingsMap();

        // STEP 3: Iterate through students and compute intersections
        // CONCEPT: Collections (ArrayList for dynamic collection of matched candidates)
        List<MatchResult> matches = new ArrayList<>();
        List<User> allUsers = userDAO.listAll();

        for (User u : allUsers) {
            // Exclude self, inactive accounts, and admins
            if (!(u instanceof Student) || u.getUserId() == requester.getUserId() || !u.isActive()) {
                continue;
            }

            Student candidate = (Student) u;
            int candidateId = candidate.getUserId();

            Set<String> candidateKnown = allKnownMap.getOrDefault(candidateId, Collections.emptySet());
            if (candidateKnown.isEmpty()) {
                continue;
            }

            // Map candidate skills to normalized comparison keys
            Map<String, String> keyToOriginalName = new HashMap<>();
            Set<String> candidateKnownNormalized = new HashSet<>();
            for (String s : candidateKnown) {
                String key = ValidationUtil.toComparisonKey(s);
                candidateKnownNormalized.add(key);
                keyToOriginalName.put(key, s);
            }

            // STEP 3b: Set intersection: commonSkills = requester.needed ∩ candidate.known
            // CONCEPT: Collections (retainAll creates mathematical set intersection)
            Set<String> intersection = new HashSet<>(neededNormalized);
            intersection.retainAll(candidateKnownNormalized);

            if (!intersection.isEmpty()) {
                // Populate display skills set
                Set<String> displayCommonSkills = new LinkedHashSet<>();
                for (String key : intersection) {
                    displayCommonSkills.add(keyToOriginalName.getOrDefault(key, key));
                }

                // STEP 4 & 5: Calculate match score
                // Score formula: (commonSkills * 10) + (averageRating * 2) + mutualBonus (15)
                double avgRating = avgRatingsMap.getOrDefault(candidateId, 0.0);
                int score = intersection.size() * 10 + (int) Math.round(avgRating * 2);

                // Detect mutual match: candidate.needed ∩ requester.known
                Set<String> candidateNeeded = allNeededMap.getOrDefault(candidateId, Collections.emptySet());
                boolean isMutual = false;
                for (String cNeed : candidateNeeded) {
                    if (requesterKnownNormalized.contains(ValidationUtil.toComparisonKey(cNeed))) {
                        isMutual = true;
                        score += 15; // Mutual synergy bonus
                        break;
                    }
                }

                // Populate candidate's full skills on model
                candidate.setSkillsKnown(candidateKnown);
                candidate.setSkillsNeeded(candidateNeeded);

                matches.add(new MatchResult(candidate, displayCommonSkills, score, avgRating, isMutual));
            }
        }

        // STEP 6: Sort using MatchResult's Comparable implementation (score desc, rating desc, name asc)
        // CONCEPT: Collections.sort & Comparable (Natural multi-tier ranking)
        Collections.sort(matches);

        return matches;
    }

    /**
     * Finds active students who know a specific subject (excluding the current user).
     */
    public List<MatchResult> searchStudentsBySubject(int excludeUserId, String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchKey = ValidationUtil.toComparisonKey(subject);
        Map<Integer, Set<String>> allKnownMap = skillDAO.getKnownSkillsByAllStudents();
        Map<Integer, Double> avgRatingsMap = ratingDAO.getAverageRatingsMap();
        List<User> allUsers = userDAO.listAll();
        List<MatchResult> results = new ArrayList<>();

        for (User u : allUsers) {
            if (!(u instanceof Student) || u.getUserId() == excludeUserId || !u.isActive()) {
                continue;
            }

            Student candidate = (Student) u;
            Set<String> known = allKnownMap.getOrDefault(candidate.getUserId(), Collections.emptySet());
            for (String s : known) {
                if (ValidationUtil.toComparisonKey(s).contains(searchKey)) {
                    double rating = avgRatingsMap.getOrDefault(candidate.getUserId(), 0.0);
                    Set<String> matchedSkill = new LinkedHashSet<>();
                    matchedSkill.add(s);
                    skillDAO.populateStudentSkills(candidate);
                    results.add(new MatchResult(candidate, matchedSkill, 10, rating, false));
                    break;
                }
            }
        }

        results.sort(Comparator
            .comparingDouble(MatchResult::getAverageRating).reversed()
            .thenComparing((MatchResult m) -> m.getStudent().getName(), String.CASE_INSENSITIVE_ORDER)
        );

        return results;
    }

    /**
     * Pure in-memory calculation method enabling isolated JUnit 5 testing without database dependencies.
     */
    public List<MatchResult> computeMatchesInMemory(Student requester, List<Student> candidates,
                                                    Map<Integer, Double> ratings) {
        if (requester == null || candidates == null) {
            return Collections.emptyList();
        }

        Set<String> neededKeys = new HashSet<>();
        for (String n : requester.getSkillsNeeded()) {
            neededKeys.add(ValidationUtil.toComparisonKey(n));
        }

        Set<String> requesterKnownKeys = new HashSet<>();
        for (String k : requester.getSkillsKnown()) {
            requesterKnownKeys.add(ValidationUtil.toComparisonKey(k));
        }

        List<MatchResult> matches = new ArrayList<>();

        for (Student c : candidates) {
            if (c.getUserId() == requester.getUserId()) {
                continue; // Exclude self
            }

            Set<String> cKnownKeys = new HashSet<>();
            Map<String, String> keyToOriginal = new HashMap<>();
            for (String k : c.getSkillsKnown()) {
                String key = ValidationUtil.toComparisonKey(k);
                cKnownKeys.add(key);
                keyToOriginal.put(key, k);
            }

            Set<String> intersection = new HashSet<>(neededKeys);
            intersection.retainAll(cKnownKeys);

            if (!intersection.isEmpty()) {
                Set<String> displayCommon = new LinkedHashSet<>();
                for (String key : intersection) {
                    displayCommon.add(keyToOriginal.getOrDefault(key, key));
                }

                double rating = ratings != null ? ratings.getOrDefault(c.getUserId(), 0.0) : 0.0;
                int score = intersection.size() * 10 + (int) Math.round(rating * 2);

                boolean mutual = false;
                for (String cn : c.getSkillsNeeded()) {
                    if (requesterKnownKeys.contains(ValidationUtil.toComparisonKey(cn))) {
                        mutual = true;
                        score += 15;
                        break;
                    }
                }

                matches.add(new MatchResult(c, displayCommon, score, rating, mutual));
            }
        }

        // Sort using MatchResult's natural ordering
        // CONCEPT: Collections.sort (Comparable)
        Collections.sort(matches);

        return matches;
    }
}
