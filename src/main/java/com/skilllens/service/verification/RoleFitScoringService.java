package com.skilllens.service.verification;

import com.skilllens.domain.model.RoleDefinition;
import com.skilllens.domain.model.RoleFitResult;
import com.skilllens.domain.model.SkillVerificationRecord;
import com.skilllens.domain.model.VerificationStatus;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Deterministic Role Fit scoring engine.
 *
 * Exact Mathematical Formula:
 * Overall Score (0-100) =
 *   (RequiredSkillCoverage * 0.40) +
 *   (VerifiedEvidenceCoverage * 0.25) +
 *   (PreferredSkillCoverage * 0.15) +
 *   (ProjectAlignmentScore * 0.10) +
 *   (EvidenceConfidenceScore * 0.10)
 *
 * Adheres strictly to the invariant: AI never calculates or alters the numeric score.
 */
@Service
public class RoleFitScoringService {

    public static final double WEIGHT_REQUIRED_SKILLS = 0.40;
    public static final double WEIGHT_VERIFIED_EVIDENCE = 0.25;
    public static final double WEIGHT_PREFERRED_SKILLS = 0.15;
    public static final double WEIGHT_PROJECT_ALIGNMENT = 0.10;
    public static final double WEIGHT_CONFIDENCE = 0.10;

    public RoleFitResult calculateRoleFit(
            RoleDefinition role,
            List<SkillVerificationRecord> verificationRecords,
            double projectRelevanceAggregate) {

        int verifiedCount = 0;
        int partiallyVerifiedCount = 0;
        int weakCount = 0;
        int unverifiedCount = 0;
        int missingCount = 0;

        double requiredPointsEarned = 0.0;
        double requiredPointsPossible = 0.0;

        double preferredPointsEarned = 0.0;
        double preferredPointsPossible = 0.0;

        double totalEvidenceStrength = 0.0;
        double totalConfidence = 0.0;
        int totalRoleSkills = 0;

        List<String> verifiedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        List<String> unverifiedClaims = new ArrayList<>();

        for (SkillVerificationRecord r : verificationRecords) {
            switch (r.status()) {
                case VERIFIED -> {
                    verifiedCount++;
                    verifiedSkills.add(r.normalizedSkill());
                }
                case PARTIALLY_VERIFIED -> {
                    partiallyVerifiedCount++;
                    verifiedSkills.add(r.normalizedSkill());
                }
                case WEAK_EVIDENCE -> {
                    weakCount++;
                    unverifiedClaims.add(r.normalizedSkill());
                }
                case CLAIMED_UNVERIFIED -> {
                    unverifiedCount++;
                    unverifiedClaims.add(r.normalizedSkill());
                }
                case MISSING -> {
                    missingCount++;
                    missingSkills.add(r.normalizedSkill());
                }
            }

            double skillWeight = role.skillWeights().getOrDefault(r.normalizedSkill(), 1.0);

            if (r.isRoleRequired()) {
                requiredPointsPossible += skillWeight;
                totalRoleSkills++;
                totalConfidence += r.confidenceScore();
                totalEvidenceStrength += r.evidenceStrength();

                if (r.status() == VerificationStatus.VERIFIED) {
                    requiredPointsEarned += skillWeight * 1.0;
                } else if (r.status() == VerificationStatus.PARTIALLY_VERIFIED) {
                    requiredPointsEarned += skillWeight * 0.70;
                } else if (r.status() == VerificationStatus.WEAK_EVIDENCE) {
                    requiredPointsEarned += skillWeight * 0.40;
                } else if (r.status() == VerificationStatus.CLAIMED_UNVERIFIED) {
                    requiredPointsEarned += skillWeight * 0.25;
                }
            } else if (r.isRolePreferred()) {
                preferredPointsPossible += skillWeight;
                totalRoleSkills++;
                totalConfidence += r.confidenceScore();
                totalEvidenceStrength += r.evidenceStrength();

                if (r.status() == VerificationStatus.VERIFIED) {
                    preferredPointsEarned += skillWeight * 1.0;
                } else if (r.status() == VerificationStatus.PARTIALLY_VERIFIED) {
                    preferredPointsEarned += skillWeight * 0.70;
                } else if (r.status() == VerificationStatus.WEAK_EVIDENCE) {
                    preferredPointsEarned += skillWeight * 0.40;
                } else if (r.status() == VerificationStatus.CLAIMED_UNVERIFIED) {
                    preferredPointsEarned += skillWeight * 0.25;
                }
            }
        }

        // Normalization to 0 - 100 percentages
        double reqCoverage = (requiredPointsPossible > 0)
                ? (requiredPointsEarned / requiredPointsPossible) * 100.0 : 100.0;
        double prefCoverage = (preferredPointsPossible > 0)
                ? (preferredPointsEarned / preferredPointsPossible) * 100.0 : 100.0;
        double verifiedCoverage = (totalRoleSkills > 0)
                ? ((double) verifiedCount / totalRoleSkills) * 100.0 : 0.0;
        double projectAlignment = Math.min(100.0, Math.max(0.0, projectRelevanceAggregate * 100.0));
        double confidenceScore = (totalRoleSkills > 0)
                ? (totalConfidence / totalRoleSkills) * 100.0 : 50.0;

        double overall = (reqCoverage * WEIGHT_REQUIRED_SKILLS)
                + (verifiedCoverage * WEIGHT_VERIFIED_EVIDENCE)
                + (prefCoverage * WEIGHT_PREFERRED_SKILLS)
                + (projectAlignment * WEIGHT_PROJECT_ALIGNMENT)
                + (confidenceScore * WEIGHT_CONFIDENCE);

        overall = Math.round(Math.min(100.0, Math.max(0.0, overall)) * 10.0) / 10.0;

        String tier;
        if (overall >= 80.0) tier = "STRONG_FIT";
        else if (overall >= 65.0) tier = "COMPETITIVE_FIT";
        else if (overall >= 50.0) tier = "DEVELOPING_FIT";
        else tier = "FOUNDATIONAL_FIT";

        String breakdown = String.format(Locale.US,
                "Formula: (%.1f%% * 0.40 Required) + (%.1f%% * 0.25 Verified Evidence) + (%.1f%% * 0.15 Preferred) + (%.1f%% * 0.10 Project Alignment) + (%.1f%% * 0.10 Confidence) = %.1f / 100",
                reqCoverage, verifiedCoverage, prefCoverage, projectAlignment, confidenceScore, overall);

        Map<String, Double> weights = Map.of(
                "RequiredSkills", WEIGHT_REQUIRED_SKILLS,
                "VerifiedEvidence", WEIGHT_VERIFIED_EVIDENCE,
                "PreferredSkills", WEIGHT_PREFERRED_SKILLS,
                "ProjectAlignment", WEIGHT_PROJECT_ALIGNMENT,
                "EvidenceConfidence", WEIGHT_CONFIDENCE
        );

        return new RoleFitResult(
                overall,
                Math.round(reqCoverage * 10.0) / 10.0,
                Math.round(verifiedCoverage * 10.0) / 10.0,
                Math.round(projectAlignment * 10.0) / 10.0,
                Math.round(prefCoverage * 10.0) / 10.0,
                Math.round(confidenceScore * 10.0) / 10.0,
                verifiedCount,
                partiallyVerifiedCount,
                weakCount,
                unverifiedCount,
                missingCount,
                tier,
                breakdown,
                weights,
                verifiedSkills,
                missingSkills,
                unverifiedClaims
        );
    }
}
