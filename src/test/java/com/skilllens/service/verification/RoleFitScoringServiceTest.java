package com.skilllens.service.verification;

import com.skilllens.domain.model.RoleDefinition;
import com.skilllens.domain.model.RoleFitResult;
import com.skilllens.domain.model.SkillVerificationRecord;
import com.skilllens.domain.model.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RoleFitScoringServiceTest {

    private RoleFitScoringService scoringService;
    private RoleDefinition testRole;

    @BeforeEach
    void setUp() {
        scoringService = new RoleFitScoringService();
        testRole = new RoleDefinition(
                "Backend Developer",
                "Designs and builds server architecture",
                "Engineering",
                List.of("Java", "Spring Boot", "SQL"),
                List.of("Docker", "AWS"),
                List.of("REST API Design"),
                List.of("Backend Service"),
                List.of("Repository with commits"),
                Map.of("Java", 1.0, "Spring Boot", 1.0, "SQL", 1.0, "Docker", 1.0, "AWS", 1.0)
        );
    }

    @Test
    @DisplayName("Calculates high score when required skills are verified with evidence")
    void testHighRoleFit() {
        List<SkillVerificationRecord> records = List.of(
                new SkillVerificationRecord("Java", VerificationStatus.VERIFIED, 0.95, 0.90, "Java experience", "10 repos", "Verified", true, false),
                new SkillVerificationRecord("Spring Boot", VerificationStatus.VERIFIED, 0.90, 0.85, "Spring Boot experience", "5 repos", "Verified", true, false),
                new SkillVerificationRecord("SQL", VerificationStatus.VERIFIED, 0.90, 0.80, "SQL experience", "8 repos", "Verified", true, false),
                new SkillVerificationRecord("Docker", VerificationStatus.PARTIALLY_VERIFIED, 0.80, 0.70, "Docker claim", "2 repos", "Partial", false, true),
                new SkillVerificationRecord("AWS", VerificationStatus.WEAK_EVIDENCE, 0.60, 0.40, "AWS claim", "1 repo", "Weak", false, true)
        );

        RoleFitResult result = scoringService.calculateRoleFit(testRole, records, 0.85);

        assertNotNull(result);
        assertTrue(result.overallScore() >= 80.0, "Score should be in strong fit range: " + result.overallScore());
        assertEquals(100.0, result.requiredSkillCoverage());
        assertEquals("STRONG_FIT", result.ratingTier());
        assertEquals(4, result.verifiedSkills().size());
        assertEquals(0, result.missingSkills().size());
    }

    @Test
    @DisplayName("Calculates low score when critical required skills are missing")
    void testLowRoleFitWithMissingSkills() {
        List<SkillVerificationRecord> records = List.of(
                new SkillVerificationRecord("Java", VerificationStatus.MISSING, 0.0, 0.0, "", "No evidence", "Missing", true, false),
                new SkillVerificationRecord("Spring Boot", VerificationStatus.MISSING, 0.0, 0.0, "", "No evidence", "Missing", true, false),
                new SkillVerificationRecord("SQL", VerificationStatus.CLAIMED_UNVERIFIED, 0.70, 0.0, "Claimed SQL", "No commits", "Unverified", true, false),
                new SkillVerificationRecord("Docker", VerificationStatus.MISSING, 0.0, 0.0, "", "No evidence", "Missing", false, true),
                new SkillVerificationRecord("AWS", VerificationStatus.MISSING, 0.0, 0.0, "", "No evidence", "Missing", false, true)
        );

        RoleFitResult result = scoringService.calculateRoleFit(testRole, records, 0.10);

        assertNotNull(result);
        assertTrue(result.overallScore() < 40.0, "Score should be low: " + result.overallScore());
        assertTrue(result.missingSkills().contains("Java"));
        assertTrue(result.missingSkills().contains("Spring Boot"));
        assertEquals("FOUNDATIONAL_FIT", result.ratingTier());
    }
}
