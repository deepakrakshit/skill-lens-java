package com.skilllens.domain.model;

import java.io.Serializable;

public record VerificationInsight(
        InsightType type,
        String headline,
        String explanation,
        String evidenceRef,
        InsightSeverity severity
) implements Serializable {

    public enum InsightType {
        STRONGEST_VERIFIED_SKILL,
        UNSUPPORTED_HIGH_STAKES_CLAIM,
        LARGEST_ROLE_GAP,
        STRONGEST_PROJECT_EVIDENCE,
        WEAKEST_EVIDENCE_AREA,
        RESUME_GITHUB_MISMATCH
    }

    public enum InsightSeverity {
        SUCCESS,
        INFO,
        WARNING
    }

    public String title() {
        return headline;
    }

    public String description() {
        return explanation;
    }
}
