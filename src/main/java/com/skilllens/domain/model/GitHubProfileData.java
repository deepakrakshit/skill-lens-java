package com.skilllens.domain.model;

import java.io.Serializable;
import java.time.Instant;

public record GitHubProfileData(
        String username,
        String name,
        String bio,
        int publicRepos,
        int followers,
        int following,
        String avatarUrl,
        String htmlUrl,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {}
