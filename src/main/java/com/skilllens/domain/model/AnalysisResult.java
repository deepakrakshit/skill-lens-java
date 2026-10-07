package com.skilllens.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The canonical, authoritative analysis result format for SkillLens.
 * Unifies candidate claims, GitHub evidence, verification matrix,
 * deterministic scoring, roadmap, and recommendations.
 */
public record AnalysisResult(
        String analysisId,
        Candidate candidate,
        RoleDefinition targetRole,
        CandidateDocument resumeDocument,
        GitHubProfileData gitHubProfile,
        List<GitHubRepoData> gitHubRepositories,
        List<GitHubCommitData> recentCommits,
        List<SkillClaim> extractedSkills,
        List<GitHubEvidenceData> gitHubEvidence,
        List<SkillVerificationRecord> verificationMatrix,
        List<SkillVerificationRecord> skillGaps,
        RoleFitResult roleFit,
        List<VerificationInsight> verificationInsights,
        List<RoadmapMonth> careerRoadmap,
        List<LearningResource> recommendedResources,
        Map<String, Object> metadata,
        Instant analyzedAt
) implements Serializable {}
