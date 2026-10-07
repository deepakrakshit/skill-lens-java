package com.skilllens.domain.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public record RoleDefinition(
        String title,
        String description,
        String category,
        List<String> requiredSkills,
        List<String> preferredSkills,
        List<String> expectedCompetencies,
        List<String> expectedProjectTypes,
        List<String> projectEvidenceExpectations,
        Map<String, Double> skillWeights
) implements Serializable {

    public String name() {
        return title;
    }
}
