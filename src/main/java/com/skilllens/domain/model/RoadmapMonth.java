package com.skilllens.domain.model;

import java.io.Serializable;
import java.util.List;

public record RoadmapMonth(
        int monthNumber,
        String title,
        String summary,
        List<String> focusSkills,
        List<String> learningObjectives,
        List<String> practicalWork,
        String projectMilestone,
        String gitHubMilestone,
        String portfolioTarget,
        List<String> recommendedResourceIds,
        List<String> successCriteria
) implements Serializable {}
