package com.skilllens.domain.model;

import java.io.Serializable;
import java.util.List;

/**
 * Recruiter candidate assessment record with fairness indicators.
 */
public record RecruiterCandidateAssessment(
        String candidateId,
        String candidateName,
        String resumeFileName,
        String githubUsername,
        boolean gitHubProvided,
        double roleFitScore, // 0 to 100
        int verifiedSkillsCount,
        int claimedSkillsCount,
        int criticalGapsCount,
        double evidenceConfidence, // 0 to 100
        String experienceSummary,
        List<String> topStrengths,
        List<String> topGaps,
        String recommendation, // HIGHLY_RECOMMENDED, RECOMMENDED, POTENTIAL_FIT, CONSIDER_WITH_RESERVATIONS
        String gitHubEvidenceStatus // "VERIFIED_ACTIVITY", "NOT_PROVIDED", "EMPTY_OR_UNAVAILABLE"
) implements Serializable {}
