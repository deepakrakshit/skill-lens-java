package com.skilllens.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Optimized resume content strictly grounded in verified candidate evidence.
 * Follows Google XYZ formula: Accomplished [X] as measured by [Y] by doing [Z].
 * Respects non-fabrication invariant.
 */
public record RewrittenResume(
        String candidateId,
        String targetRole,
        String professionalSummary,
        List<RewrittenExperience> experiences,
        List<RewrittenProject> projects,
        List<String> prioritizedSkills,
        ResumeTemplateType templateType,
        Instant createdAt
) implements Serializable {

    public record RewrittenExperience(
            String company,
            String title,
            String period,
            List<String> originalBullets,
            List<String> rewrittenBullets,
            List<String> verifiedSkillTags
    ) implements Serializable {}

    public record RewrittenProject(
            String projectName,
            String repositoryUrl,
            String originalDescription,
            String rewrittenDescription,
            List<String> highlights,
            List<String> verifiedTechnologies
    ) implements Serializable {}
}
