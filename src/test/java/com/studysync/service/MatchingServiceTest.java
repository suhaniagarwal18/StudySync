package com.studysync.service;

import com.studysync.model.MatchResult;
import com.studysync.model.SkillType;
import com.studysync.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure in-memory unit tests verifying the peer study buddy matching algorithm.
 * 
 * CONCEPT: Algorithm Verification & Collections Unit Testing
 * WHY: Tests set intersection, mutual bonus calculation, self-exclusion,
 * and comparator ranking completely in-memory without database dependencies.
 */
class MatchingServiceTest {

    private MatchingService matchingService;
    private Student studentA; // Aarav: Knows Python, DBMS | Needs Java
    private Student studentB; // Diya:  Knows Java, DSA    | Needs DBMS
    private Student studentC; // Rohan: Knows OS, Networks | Needs Python
    private List<Student> allCandidates;
    private Map<Integer, Double> ratings;

    @BeforeEach
    void setUp() throws Exception {
        matchingService = new MatchingService();

        studentA = new Student(1, "Aarav Sharma", "aarav@studysync.edu", "hash", "CS");
        studentA.addSkill("Python", SkillType.KNOWN);
        studentA.addSkill("DBMS", SkillType.KNOWN);
        studentA.addSkill("Java", SkillType.NEEDED);

        studentB = new Student(2, "Diya Patel", "diya@studysync.edu", "hash", "IT");
        studentB.addSkill("Java", SkillType.KNOWN);
        studentB.addSkill("DSA", SkillType.KNOWN);
        studentB.addSkill("DBMS", SkillType.NEEDED);

        studentC = new Student(3, "Rohan Verma", "rohan@studysync.edu", "hash", "Electronics");
        studentC.addSkill("Operating Systems", SkillType.KNOWN);
        studentC.addSkill("Computer Networks", SkillType.KNOWN);
        studentC.addSkill("Python", SkillType.NEEDED);

        allCandidates = new ArrayList<>();
        allCandidates.add(studentA);
        allCandidates.add(studentB);
        allCandidates.add(studentC);

        ratings = new HashMap<>();
        ratings.put(1, 4.8);
        ratings.put(2, 5.0);
        ratings.put(3, 4.2);
    }

    @Test
    @DisplayName("Requester A (needs Java) should match with Diya B (knows Java) with mutual bonus")
    void testMutualMatchDiscovery() {
        List<MatchResult> matches = matchingService.computeMatchesInMemory(studentA, allCandidates, ratings);

        assertNotNull(matches);
        assertFalse(matches.isEmpty(), "Should find matches for student A");

        // Diya should be the top match
        MatchResult topMatch = matches.get(0);
        assertEquals(studentB.getUserId(), topMatch.getStudent().getUserId());
        assertTrue(topMatch.getCommonSkills().contains("Java"));
        assertTrue(topMatch.isMutualMatch(), "Match between A and B must be flagged as a MUTUAL match");

        // Score calculation: 1 common skill * 10 = 10, rating 5.0 * 2 = 10, mutual bonus = 15 => Total 35
        assertEquals(35, topMatch.getMatchScore());
    }

    @Test
    @DisplayName("Matching algorithm must strictly exclude the requester themselves")
    void testExcludeSelf() {
        List<MatchResult> matches = matchingService.computeMatchesInMemory(studentA, allCandidates, ratings);

        for (MatchResult m : matches) {
            assertFalse(m.getStudent().getUserId() == studentA.getUserId(),
                "Requester should never appear in their own buddy recommendations");
        }
    }

    @Test
    @DisplayName("Should return empty list when no candidates possess the needed skills")
    void testNoMatch() throws Exception {
        Student isolatedStudent = new Student(99, "Isolated Student", "iso@studysync.edu", "hash", "Math");
        isolatedStudent.addSkill("Quantum Mechanics", SkillType.NEEDED);

        List<MatchResult> matches = matchingService.computeMatchesInMemory(isolatedStudent, allCandidates, ratings);
        assertTrue(matches.isEmpty(), "Should return empty list when no candidate has matching skills");
    }

    @Test
    @DisplayName("Ranking order: higher match score appears first")
    void testRankingByScore() throws Exception {
        // Create candidate with 2 matching skills
        Student studentMulti = new Student(4, "Multi Skill Student", "multi@studysync.edu", "hash", "CS");
        studentMulti.addSkill("Java", SkillType.KNOWN);
        studentMulti.addSkill("DBMS", SkillType.NEEDED); // mutual

        allCandidates.add(studentMulti);

        List<MatchResult> matches = matchingService.computeMatchesInMemory(studentA, allCandidates, ratings);
        assertTrue(matches.size() >= 2);

        // Verify descending score order
        for (int i = 0; i < matches.size() - 1; i++) {
            assertTrue(matches.get(i).getMatchScore() >= matches.get(i + 1).getMatchScore(),
                "Results must be sorted by matchScore descending");
        }
    }
}
