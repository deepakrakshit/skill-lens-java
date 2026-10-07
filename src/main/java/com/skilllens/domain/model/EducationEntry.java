package com.skilllens.domain.model;

import java.io.Serializable;
import java.util.List;

public record EducationEntry(
        String degree,
        String institution,
        String fieldOfStudy,
        String graduationYear,
        String gpa
) implements Serializable {}
