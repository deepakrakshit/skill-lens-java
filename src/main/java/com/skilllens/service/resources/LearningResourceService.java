package com.skilllens.service.resources;

import com.skilllens.domain.model.LearningResource;
import com.skilllens.service.skills.SkillTaxonomyService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service orchestrating learning resource discovery, quality scoring, and curriculum ranking.
 */
@Service
public class LearningResourceService {

    private final OfflineResourceCatalog offlineCatalog;
    private final YouTubeClient youTubeClient;
    private final SkillTaxonomyService taxonomyService;

    public LearningResourceService(OfflineResourceCatalog offlineCatalog,
                                   YouTubeClient youTubeClient,
                                   SkillTaxonomyService taxonomyService) {
        this.offlineCatalog = offlineCatalog;
        this.youTubeClient = youTubeClient;
        this.taxonomyService = taxonomyService;
    }

    /**
     * Resolves and ranks the best learning resources for a list of target skills and role.
     */
    public List<LearningResource> getResourcesForGaps(List<String> missingSkills, String targetRole, int maxPerSkill) {
        if (missingSkills == null || missingSkills.isEmpty()) {
            return offlineCatalog.getAllResources().stream().limit(8).toList();
        }

        List<LearningResource> allGathered = new ArrayList<>();
        int monthAssign = 1;

        for (String skill : missingSkills) {
            String norm = taxonomyService.normalize(skill);

            // 1. Gather curated offline resources
            List<LearningResource> offlineResources = offlineCatalog.getResourcesForSkill(norm);
            allGathered.addAll(offlineResources);

            // 2. Fetch YouTube resources (if configured)
            List<LearningResource> ytResources = youTubeClient.searchResourcesForSkill(skill, targetRole, monthAssign);
            allGathered.addAll(ytResources);

            monthAssign = (monthAssign % 4) + 1;
        }

        // Rank resources by QualityScore
        return rankResources(allGathered, missingSkills, targetRole);
    }

    private List<LearningResource> rankResources(List<LearningResource> resources, List<String> targetSkills, String role) {
        Set<String> targetSkillSet = new HashSet<>();
        for (String s : targetSkills) {
            targetSkillSet.add(s.toLowerCase(Locale.ROOT));
        }

        return resources.stream()
                .distinct()
                .sorted(Comparator.comparingDouble((LearningResource r) -> calculateQualityScore(r, targetSkillSet, role)).reversed())
                .toList();
    }

    /**
     * QualityScore = 0.40 * TopicRelevance + 0.25 * Completeness + 0.20 * Reputation + 0.15 * LevelFit
     */
    private double calculateQualityScore(LearningResource r, Set<String> targetSkills, String role) {
        // Topic relevance
        double relevance = 0.5;
        if (targetSkills.contains(r.skill().toLowerCase(Locale.ROOT))) {
            relevance = 1.0;
        }

        // Completeness
        double completeness = switch (r.type()) {
            case PLAYLIST, DOCUMENTATION -> 1.0;
            case PROJECT_GUIDE -> 0.9;
            case INTERACTIVE_TUTORIAL -> 0.8;
            case VIDEO -> 0.7;
        };

        // Channel / Source Reputation
        double reputation = switch (r.provider()) {
            case "OFFICIAL_DOCS" -> 1.0;
            case "GITHUB" -> 0.9;
            case "FREECODECAMP", "ROADMAP_SH" -> 0.85;
            case "YOUTUBE" -> 0.80;
            default -> 0.70;
        };

        // Level Fit
        double levelFit = 0.85; // intermediate baseline

        return (0.40 * relevance) + (0.25 * completeness) + (0.20 * reputation) + (0.15 * levelFit);
    }
}
