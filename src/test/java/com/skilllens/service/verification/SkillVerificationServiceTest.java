package com.skilllens.service.verification;

import com.skilllens.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SkillVerificationServiceTest {

    private SkillVerificationService verificationService;
    private RoleDefinition targetRole;

    @BeforeEach
    void setUp() {
        verificationService = new SkillVerificationService();
        targetRole = new RoleDefinition(
                "Backend Developer",
                "Backend engineering role",
                "Engineering",
                List.of("Java", "Spring Boot", "SQL"),
                List.of("Docker", "AWS"),
                List.of("API Design"),
                List.of("Microservices"),
                List.of("Repository with commits"),
                Map.of("Java", 1.2, "Spring Boot", 1.2, "SQL", 1.0)
        );
    }

    @Test
    @DisplayName("Assigns VERIFIED when both resume claim and strong GitHub evidence exist")
    void testVerifiedStatus() {
        List<SkillClaim> claims = List.of(
                new SkillClaim("Java", "java", "Languages", "3 years Java experience", 0.95, "REGEX")
        );
        List<GitHubEvidenceData> evidence = List.of(
                new GitHubEvidenceData("java", 5, List.of("backend-api", "microservice"), 45, List.of("pom.xml"), true, 2, 0.92)
        );

        List<SkillVerificationRecord> records = verificationService.evaluateSkills(claims, evidence, targetRole);

        SkillVerificationRecord javaRecord = records.stream()
                .filter(r -> r.normalizedSkill().equalsIgnoreCase("java"))
                .findFirst()
                .orElse(null);

        assertNotNull(javaRecord);
        assertEquals(VerificationStatus.VERIFIED, javaRecord.status());
        assertTrue(javaRecord.evidenceStrength() > 0.80);
    }

    @Test
    @DisplayName("Assigns CLAIMED_UNVERIFIED when resume claims skill but zero GitHub evidence exists")
    void testClaimedUnverifiedStatus() {
        List<SkillClaim> claims = List.of(
                new SkillClaim("Spring Boot", "spring boot", "Frameworks", "Spring Boot developer", 0.90, "REGEX")
        );
        List<GitHubEvidenceData> evidence = List.of();

        List<SkillVerificationRecord> records = verificationService.evaluateSkills(claims, evidence, targetRole);

        SkillVerificationRecord springRecord = records.stream()
                .filter(r -> r.normalizedSkill().equalsIgnoreCase("spring boot"))
                .findFirst()
                .orElse(null);

        assertNotNull(springRecord);
        assertEquals(VerificationStatus.CLAIMED_UNVERIFIED, springRecord.status());
        assertEquals(0.0, springRecord.evidenceStrength());
    }

    @Test
    @DisplayName("Assigns MISSING when target role requires skill but candidate neither claimed nor provided evidence")
    void testMissingStatus() {
        List<SkillClaim> claims = List.of();
        List<GitHubEvidenceData> evidence = List.of();

        List<SkillVerificationRecord> records = verificationService.evaluateSkills(claims, evidence, targetRole);

        SkillVerificationRecord sqlRecord = records.stream()
                .filter(r -> r.normalizedSkill().equalsIgnoreCase("sql"))
                .findFirst()
                .orElse(null);

        assertNotNull(sqlRecord);
        assertEquals(VerificationStatus.MISSING, sqlRecord.status());
        assertTrue(sqlRecord.isRoleRequired());
    }
}
