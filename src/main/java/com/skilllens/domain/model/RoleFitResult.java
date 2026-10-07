package com.skilllens.domain.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record RoleFitResult(
        double overallScore, // 0 to 100
        double requiredSkillCoverage, // 0 to 100
        double verifiedEvidenceCoverage, // 0 to 100
        double projectAlignmentScore, // 0 to 100
        double preferredSkillCoverage, // 0 to 100
        double evidenceConfidenceScore, // 0 to 100
        int verifiedCount,
        int partiallyVerifiedCount,
        int weakEvidenceCount,
        int unverifiedClaimCount,
        int missingCount,
        String ratingTier, // STRONG_FIT, COMPETITIVE_FIT, DEVELOPING_FIT, FOUNDATIONAL_FIT
        String formulaBreakdown,
        Map<String, Double> componentWeights,
        List<String> verifiedSkills,
        List<String> missingSkills,
        List<String> unverifiedClaims
) implements Serializable {

    public RoleFitResult(
            double overallScore,
            double requiredSkillCoverage,
            double verifiedEvidenceCoverage,
            double projectAlignmentScore,
            double preferredSkillCoverage,
            double evidenceConfidenceScore,
            int verifiedCount,
            int partiallyVerifiedCount,
            int weakEvidenceCount,
            int unverifiedClaimCount,
            int missingCount,
            String ratingTier,
            String formulaBreakdown,
            Map<String, Double> componentWeights) {
        this(overallScore, requiredSkillCoverage, verifiedEvidenceCoverage, projectAlignmentScore,
                preferredSkillCoverage, evidenceConfidenceScore, verifiedCount, partiallyVerifiedCount,
                weakEvidenceCount, unverifiedClaimCount, missingCount, ratingTier, formulaBreakdown,
                componentWeights, List.of(), List.of(), List.of());
    }

    public double verifiedSkillCoverage() {
        return verifiedEvidenceCoverage;
    }
}
