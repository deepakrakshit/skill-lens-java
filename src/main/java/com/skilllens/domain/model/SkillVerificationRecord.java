package com.skilllens.domain.model;

import java.io.Serializable;

public record SkillVerificationRecord(
        String normalizedSkill,
        VerificationStatus status,
        double confidenceScore, // 0.0 to 1.0
        double evidenceStrength, // 0.0 to 1.0
        String resumeClaimText,
        String gitHubEvidenceSummary,
        String explanation,
        boolean isRoleRequired,
        boolean isRolePreferred
) implements Serializable {

    public String canonicalSkill() {
        return normalizedSkill;
    }
}
