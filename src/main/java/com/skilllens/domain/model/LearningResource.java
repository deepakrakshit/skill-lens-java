package com.skilllens.domain.model;

import java.io.Serializable;

public record LearningResource(
        String id,
        String title,
        String description,
        String url,
        String provider, // YOUTUBE, OFFICIAL_DOCS, ROADMAP_SH, FREECODECAMP, GITHUB
        ResourceType type,
        String skill,
        String difficulty, // BEGINNER, INTERMEDIATE, ADVANCED
        double rating, // 0.0 to 5.0
        String estimatedDuration,
        int roadmapMonth,
        boolean isOfflineAvailable
) implements Serializable {

    public enum ResourceType {
        VIDEO,
        PLAYLIST,
        DOCUMENTATION,
        INTERACTIVE_TUTORIAL,
        PROJECT_GUIDE
    }
}
