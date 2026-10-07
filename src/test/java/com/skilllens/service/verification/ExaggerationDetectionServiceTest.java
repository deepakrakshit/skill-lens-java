package com.skilllens.service.verification;

import com.skilllens.domain.model.SkillVerificationRecord;
import com.skilllens.domain.model.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExaggerationDetectionServiceTest {

    private ExaggerationDetectionService detectionService;

    @BeforeEach
    void setUp() {
        detectionService = new ExaggerationDetectionService();
    }

    @Test
    @DisplayName("Detects superlative claims that exceed available public GitHub evidence")
    void testDetectExaggerations() {
        String resumeText = "I am a recognized Docker expert with deep expertise in Kubernetes and enterprise scale.";
        List<SkillVerificationRecord> records = List.of(
                new SkillVerificationRecord("Docker", VerificationStatus.CLAIMED_UNVERIFIED, 0.8, 0.0, "Docker expert", "No repos", "Unverified", false, false),
                new SkillVerificationRecord("Kubernetes", VerificationStatus.WEAK_EVIDENCE, 0.6, 0.2, "deep expertise in Kubernetes", "1 commit", "Weak", false, false)
        );

        List<ExaggerationDetectionService.ExaggerationFlag> flags = detectionService.detectExaggerations(resumeText, records);

        assertNotNull(flags);
        assertFalse(flags.isEmpty(), "Should detect at least one exaggeration flag");
        assertTrue(flags.stream().anyMatch(f -> f.skill().equalsIgnoreCase("Docker") || f.skill().equalsIgnoreCase("Kubernetes")));
    }
}
