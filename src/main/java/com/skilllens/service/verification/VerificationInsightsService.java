package com.skilllens.service.verification;

import com.skilllens.domain.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Extracts high-value actionable insights from verification results.
 * Limits output to high-signal observations rather than overwhelming dashboards.
 */
@Service
public class VerificationInsightsService {

    public List<VerificationInsight> generateInsights(
            List<SkillVerificationRecord> records,
            List<GitHubRepoData> repos,
            RoleDefinition role) {

        List<VerificationInsight> insights = new ArrayList<>();

        // 1. Strongest Verified Skill
        records.stream()
                .filter(r -> r.status() == VerificationStatus.VERIFIED)
                .max(Comparator.comparingDouble(SkillVerificationRecord::evidenceStrength))
                .ifPresent(r -> insights.add(new VerificationInsight(
                        VerificationInsight.InsightType.STRONGEST_VERIFIED_SKILL,
                        "Strongest Verified Competency: " + r.normalizedSkill(),
                        "Strong practical evidence found across public repositories and commit logs: " + r.gitHubEvidenceSummary(),
                        r.normalizedSkill(),
                        VerificationInsight.InsightSeverity.SUCCESS
                )));

        // 2. Largest Role-Critical Gap
        records.stream()
                .filter(r -> r.isRoleRequired() && (r.status() == VerificationStatus.MISSING || r.status() == VerificationStatus.CLAIMED_UNVERIFIED))
                .findFirst()
                .ifPresent(r -> insights.add(new VerificationInsight(
                        VerificationInsight.InsightType.LARGEST_ROLE_GAP,
                        "Critical Role Gap: " + r.normalizedSkill(),
                        r.normalizedSkill() + " is a core requirement for " + role.title() + ", but lacks verifiable public repository evidence.",
                        r.normalizedSkill(),
                        VerificationInsight.InsightSeverity.WARNING
                )));

        // 3. Strongest Project Evidence
        if (!repos.isEmpty()) {
            GitHubRepoData topRepo = repos.getFirst();
            insights.add(new VerificationInsight(
                    VerificationInsight.InsightType.STRONGEST_PROJECT_EVIDENCE,
                    "Top Relevant Repository: " + topRepo.name(),
                    "Relevance score " + (int)(topRepo.relevanceScore() * 100) + "%: " + String.join("; ", topRepo.relevanceReasons()),
                    topRepo.htmlUrl(),
                    VerificationInsight.InsightSeverity.SUCCESS
            ));
        }

        // 4. Unsupported High-Stakes Claim
        records.stream()
                .filter(r -> r.isRoleRequired() && r.status() == VerificationStatus.CLAIMED_UNVERIFIED)
                .findFirst()
                .ifPresent(r -> insights.add(new VerificationInsight(
                        VerificationInsight.InsightType.UNSUPPORTED_HIGH_STAKES_CLAIM,
                        "Resume Claim Exceeds Evidence: " + r.normalizedSkill(),
                        "Claimed on resume, but no public repositories or commits currently support practical application. " +
                                "Building a dedicated demonstration project is recommended.",
                        r.normalizedSkill(),
                        VerificationInsight.InsightSeverity.INFO
                )));

        // 5. Weakest Evidence Area
        records.stream()
                .filter(r -> r.status() == VerificationStatus.WEAK_EVIDENCE)
                .findFirst()
                .ifPresent(r -> insights.add(new VerificationInsight(
                        VerificationInsight.InsightType.WEAKEST_EVIDENCE_AREA,
                        "Emerging / Weak Evidence: " + r.normalizedSkill(),
                        "Minimal public evidence detected in repository metadata. Increasing commit recency would strengthen this profile.",
                        r.normalizedSkill(),
                        VerificationInsight.InsightSeverity.INFO
                )));

        return insights;
    }
}
