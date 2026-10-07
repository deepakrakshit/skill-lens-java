package com.skilllens.domain.model;

import java.io.Serializable;
import java.util.List;

public record ProjectEntry(
        String name,
        String description,
        String repoUrl,
        String liveUrl,
        List<String> technologies,
        List<String> highlights
) implements Serializable {}
