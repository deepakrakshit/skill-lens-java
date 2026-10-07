package com.skilllens.service.recruiter;

import com.skilllens.domain.model.RecruiterCandidateAssessment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CandidateRankingServiceTest {

    private CandidateRankingService rankingService;

    @BeforeEach
    void setUp() {
        rankingService = new CandidateRankingService();
    }

    @Test
    @DisplayName("Ranks candidates descending by composite role fit score")
    void testRankingOrder() {
        RecruiterCandidateAssessment candA = new RecruiterCandidateAssessment(
                "1", "Alice", "alice.pdf", "alicegh", true, 88.5, 8, 10, 1, 90.0,
                "Strong backend engineer", List.of("Java", "Spring"), List.of(), "HIGHLY_RECOMMENDED", "VERIFIED_ACTIVITY"
        );
        RecruiterCandidateAssessment candB = new RecruiterCandidateAssessment(
                "2", "Bob", "bob.pdf", "bobgh", true, 62.0, 4, 8, 3, 60.0,
                "Junior engineer", List.of("Python"), List.of("SQL"), "POTENTIAL_FIT", "VERIFIED_ACTIVITY"
        );
        RecruiterCandidateAssessment candC = new RecruiterCandidateAssessment(
                "3", "Charlie", "charlie.pdf", null, false, 74.0, 6, 9, 2, 70.0,
                "Experienced engineer without public GitHub", List.of("Java"), List.of(), "RECOMMENDED", "NOT_PROVIDED"
        );

        List<RecruiterCandidateAssessment> ranked = rankingService.rankCandidates(List.of(candB, candA, candC));

        assertEquals("Alice", ranked.get(0).candidateName());
        assertEquals("Charlie", ranked.get(1).candidateName());
        assertEquals("Bob", ranked.get(2).candidateName());
    }

    @Test
    @DisplayName("Enforces fairness rule: candidate without GitHub receives valid recommendation based on available evidence")
    void testFairnessPolicyRecommendation() {
        String rec = rankingService.determineRecommendation(75.0, 70.0, false);
        assertEquals("RECOMMENDED", rec);

        String strongRec = rankingService.determineRecommendation(85.0, 80.0, true);
        assertEquals("HIGHLY_RECOMMENDED", strongRec);
    }
}
