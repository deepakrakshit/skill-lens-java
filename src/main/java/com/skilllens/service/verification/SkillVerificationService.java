package com.skilllens.service.verification;

import com.skilllens.domain.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Multi-signal skill verification service implementing the 5-tier evidence matrix:
 * 1. VERIFIED
 * 2. PARTIALLY_VERIFIED
 * 3. WEAK_EVIDENCE
 * 4. CLAIMED_UNVERIFIED
 * 5. MISSING
 *
 * Grounded in empirical public signals; strictly avoids accusatory language.
 */
@Service
public class SkillVerificationService {

    public List<SkillVerificationRecord> evaluateSkills(
            List<SkillClaim> resumeClaims,
            List<GitHubEvidenceData> gitHubEvidence,
            RoleDefinition role) {

        Map<String, SkillClaim> claimMap = new HashMap<>();
        for (SkillClaim sc : resumeClaims) {
            claimMap.put(sc.normalizedSkill().toLowerCase(), sc);
        }

        Map<String, GitHubEvidenceData> evidenceMap = new HashMap<>();
        for (GitHubEvidenceData ge : gitHubEvidence) {
            evidenceMap.put(ge.normalizedSkill().toLowerCase(), ge);
        }

        Set<String> allSkillsToEvaluate = new LinkedHashSet<>();

        // First add all required skills
        for (String req : role.requiredSkills()) {
            allSkillsToEvaluate.add(req);
        }
        // Then preferred skills
        for (String pref : role.preferredSkills()) {
            allSkillsToEvaluate.add(pref);
        }
        // Then all candidate claimed skills
        for (SkillClaim sc : resumeClaims) {
            allSkillsToEvaluate.add(sc.normalizedSkill());
        }

        List<SkillVerificationRecord> records = new ArrayList<>();

        for (String skillName : allSkillsToEvaluate) {
            String lower = skillName.toLowerCase();
            boolean isClaimed = claimMap.containsKey(lower);
            boolean hasEvidence = evidenceMap.containsKey(lower);
            boolean isRequired = role.requiredSkills().stream().anyMatch(s -> s.equalsIgnoreCase(skillName));
            boolean isPreferred = role.preferredSkills().stream().anyMatch(s -> s.equalsIgnoreCase(skillName));

            SkillClaim claim = claimMap.get(lower);
            GitHubEvidenceData evidence = evidenceMap.get(lower);

            String claimSnippet = (claim != null) ? claim.sourceSnippet() : "Not claimed on resume";
            String evidenceSummary;
            double evidenceStrength = 0.0;
            double confidence;
            VerificationStatus status;
            String explanation;

            if (isClaimed && hasEvidence) {
                evidenceStrength = evidence.evidenceStrength();
                evidenceSummary = formatEvidenceSummary(evidence);

                if (evidenceStrength >= 0.60) {
                    status = VerificationStatus.VERIFIED;
                    confidence = 0.90;
                    explanation = "Direct repository codebase presence, build manifest configuration, and recent commit activity verify practical application.";
                } else if (evidenceStrength >= 0.30) {
                    status = VerificationStatus.PARTIALLY_VERIFIED;
                    confidence = 0.70;
                    explanation = "Repository presence confirmed; however, commit depth or manifest declarations are moderate.";
                } else {
                    status = VerificationStatus.WEAK_EVIDENCE;
                    confidence = 0.50;
                    explanation = "Minimal public evidence detected in repository metadata or commits.";
                }
            } else if (isClaimed && !hasEvidence) {
                evidenceSummary = "No corresponding public repository or commit evidence found";
                evidenceStrength = 0.0;
                status = VerificationStatus.CLAIMED_UNVERIFIED;
                confidence = 0.35;
                explanation = "Skill claimed on resume; however, claim is not supported by available public GitHub repositories or commits.";
            } else if (!isClaimed && hasEvidence) {
                evidenceStrength = evidence.evidenceStrength();
                evidenceSummary = formatEvidenceSummary(evidence);
                status = VerificationStatus.PARTIALLY_VERIFIED;
                confidence = 0.65;
                explanation = "Active public GitHub project evidence discovered, although skill was not explicitly claimed on resume.";
            } else {
                // Not claimed, no evidence
                evidenceSummary = "None";
                evidenceStrength = 0.0;
                status = VerificationStatus.MISSING;
                confidence = 0.0;
                explanation = "Skill is a core role competency, but is absent from both resume claims and public repositories.";
            }

            records.add(new SkillVerificationRecord(
                    skillName,
                    status,
                    confidence,
                    evidenceStrength,
                    claimSnippet,
                    evidenceSummary,
                    explanation,
                    isRequired,
                    isPreferred
            ));
        }

        return records;
    }

    private String formatEvidenceSummary(GitHubEvidenceData evidence) {
        StringBuilder sb = new StringBuilder();
        sb.append(evidence.matchingRepoCount()).append(" repos (").append(String.join(", ", evidence.relevantRepositories())).append(")");
        if (evidence.matchingCommitCount() > 0) {
            sb.append(", ").append(evidence.matchingCommitCount()).append(" matching commits");
        }
        if (!evidence.manifestFilesFound().isEmpty()) {
            sb.append(", manifests: ").append(String.join(", ", evidence.manifestFilesFound()));
        }
        return sb.toString();
    }
}
