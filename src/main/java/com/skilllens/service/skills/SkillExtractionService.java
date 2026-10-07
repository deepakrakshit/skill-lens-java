package com.skilllens.service.skills;

import com.skilllens.domain.model.SkillClaim;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts skills from resume text with full source provenance,
 * normalization, and confidence estimation.
 */
@Service
public class SkillExtractionService {

    private final SkillTaxonomyService taxonomyService;

    public SkillExtractionService(SkillTaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    public List<SkillClaim> extractSkills(String resumeText) {
        if (resumeText == null || resumeText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, SkillClaim> extracted = new LinkedHashMap<>();
        String[] lines = resumeText.split("\\r?\\n");

        boolean inSkillsSection = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String lowerLine = trimmed.toLowerCase();
            if (lowerLine.contains("skill") || lowerLine.contains("technolog") || lowerLine.contains("competenc") || lowerLine.contains("tools")) {
                inSkillsSection = true;
            } else if (trimmed.matches("^[A-Z\\s]{4,30}$") && !lowerLine.contains("skill")) {
                // Another major section header (e.g., EXPERIENCE, EDUCATION)
                inSkillsSection = false;
            }

            for (String canonical : taxonomyService.getAllCanonicalSkills()) {
                String norm = taxonomyService.normalize(canonical);
                if (extracted.containsKey(norm)) continue;

                // Word boundary pattern
                Pattern pattern = Pattern.compile("\\b" + Pattern.quote(canonical) + "\\b", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(trimmed);

                if (matcher.find()) {
                    String snippet = extractSnippet(trimmed, matcher.start(), matcher.end());
                    double confidence = inSkillsSection ? 0.95 : 0.80;
                    String category = taxonomyService.getCategory(norm);

                    extracted.put(norm, new SkillClaim(
                            canonical,
                            norm,
                            category,
                            snippet,
                            confidence,
                            inSkillsSection ? "SKILLS_SECTION_REGEX" : "CONTEXTUAL_TEXT_MATCH"
                    ));
                }
            }
        }

        return new ArrayList<>(extracted.values());
    }

    private String extractSnippet(String line, int start, int end) {
        int left = Math.max(0, start - 30);
        int right = Math.min(line.length(), end + 30);
        String prefix = left > 0 ? "..." : "";
        String suffix = right < line.length() ? "..." : "";
        return prefix + line.substring(left, right).trim() + suffix;
    }
}
