package com.skilllens.domain.model;

import java.io.Serializable;

/**
 * Record representing a skill claimed by the candidate on their resume.
 * Maintains full provenance back to the source text.
 */
public record SkillClaim(
        String rawSkill,
        String normalizedSkill,
        String category,
        String sourceSnippet,
        double confidence,
        String extractionMethod // REGEX, TAXONOMY, NLP, VISION
) implements Serializable {
    public SkillClaim {
        if (normalizedSkill == null || normalizedSkill.trim().isEmpty()) {
            normalizedSkill = (rawSkill != null) ? rawSkill.trim() : "Unknown";
        }
        if (category == null) {
            category = "General Technical";
        }
        if (extractionMethod == null) {
            extractionMethod = "TAXONOMY";
        }
    }

    public String canonicalSkill() {
        return rawSkill != null ? rawSkill : normalizedSkill;
    }

    public String contextSnippet() {
        return sourceSnippet;
    }
}
