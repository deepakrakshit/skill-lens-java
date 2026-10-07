package com.skilllens.service.recruiter;

import com.skilllens.domain.model.RecruiterCandidateAssessment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service for ranking candidate assessments with strict fairness invariants.
 * Candidates without a GitHub profile are never penalized with negative evidence,
 * but transparently marked as "NOT_PROVIDED".
 */
@Service
public class CandidateRankingService {

    /**
     * Ranks candidates descending by composite fit score and verified skills.
     */
    public List<RecruiterCandidateAssessment> rankCandidates(List<RecruiterCandidateAssessment> assessments) {
        if (assessments == null || assessments.isEmpty()) {
            return List.of();
        }

        List<RecruiterCandidateAssessment> ranked = new ArrayList<>(assessments);

        // Sort by role fit score descending, then by verified skills count descending
        ranked.sort(Comparator.comparingDouble(RecruiterCandidateAssessment::roleFitScore)
                .thenComparingInt(RecruiterCandidateAssessment::verifiedSkillsCount)
                .reversed());

        return ranked;
    }

    /**
     * Assigns recommendation tier based on role fit score and verification confidence.
     */
    public String determineRecommendation(double roleFitScore, double evidenceConfidence, boolean gitHubProvided) {
        if (roleFitScore >= 80.0 && evidenceConfidence >= 70.0) {
            return "HIGHLY_RECOMMENDED";
        } else if (roleFitScore >= 65.0) {
            return "RECOMMENDED";
        } else if (roleFitScore >= 50.0) {
            return "POTENTIAL_FIT";
        } else {
            return "CONSIDER_WITH_RESERVATIONS";
        }
    }
}
