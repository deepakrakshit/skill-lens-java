package com.skilllens.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record GitHubRepoData(
        long id,
        String name,
        String fullName,
        String description,
        String htmlUrl,
        String defaultBranch,
        String primaryLanguage,
        Map<String, Long> languagesMap,
        List<String> topics,
        int stars,
        int forks,
        boolean isFork,
        boolean isArchived,
        Instant updatedAt,
        Instant pushedAt,
        long sizeKb,
        double relevanceScore, // 0.0 to 1.0
        List<String> relevanceReasons
) implements Serializable {}
