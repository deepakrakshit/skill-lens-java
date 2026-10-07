package com.skilllens.domain.model;

import java.io.Serializable;
import java.util.List;

public record ExperienceEntry(
        String company,
        String title,
        String startDate,
        String endDate,
        String description,
        List<String> highlights,
        List<String> technologies
) implements Serializable {}
