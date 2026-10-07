package com.skilllens.domain.model;

import java.io.Serializable;
import java.util.List;

public record GitHubEvidenceData(
        String normalizedSkill,
        int matchingRepoCount,
        List<String> relevantRepositories,
        int matchingCommitCount,
        List<String> manifestFilesFound, // e.g. ["pom.xml", "Dockerfile", "package.json"]
        boolean codePresenceVerified,
        int recencyMonths,
        double evidenceStrength // 0.0 to 1.0
) implements Serializable {

    public String canonicalSkill() {
        return normalizedSkill;
    }

    public List<String> matchedRepositories() {
        return relevantRepositories != null ? relevantRepositories : List.of();
    }

    public List<String> matchedCommitMessages() {
        return List.of();
    }

    public String evidenceSummary() {
        return matchingRepoCount + " repositories, " + matchingCommitCount + " commits";
    }
}
