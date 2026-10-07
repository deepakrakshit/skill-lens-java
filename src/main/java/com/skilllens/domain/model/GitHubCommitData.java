package com.skilllens.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record GitHubCommitData(
        String sha,
        String message,
        String authorName,
        String authorEmail,
        Instant authorDate,
        Instant commitDate,
        String htmlUrl,
        String branch,
        int filesChanged,
        int additions,
        int deletions,
        List<String> impactedTechFiles
) implements Serializable {}
