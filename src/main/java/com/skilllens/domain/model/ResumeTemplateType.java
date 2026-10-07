package com.skilllens.domain.model;

/**
 * 10 distinct professional ATS-friendly resume templates.
 */
public enum ResumeTemplateType {
    CLASSIC_PROFESSIONAL("Classic Professional", "Traditional serif headers, structured chronologies, high corporate readability."),
    MODERN_MINIMAL("Modern Minimal", "Clean sans-serif typography, generous whitespace, subtle accents."),
    SOFTWARE_ENGINEER("Software Engineer", "Prominent technical stack bar, verified GitHub repo links, XYZ metric bullets."),
    TECHNICAL_COMPACT("Technical Compact", "High-density two-column layout optimized for senior engineering breadth."),
    EXECUTIVE_CLEAN("Executive Clean", "Strategic leadership summaries, business impact metrics, elegant hierarchy."),
    PRODUCT_ENGINEERING("Product/Engineering", "Balanced technical competencies and cross-functional delivery metrics."),
    DATA_AND_ANALYTICS("Data & Analytics", "Dedicated sections for ML frameworks, statistical methodologies, and data pipelines."),
    ACADEMIC_TECHNICAL("Academic Technical", "Extended publication, research thesis, and foundational coursework focus."),
    STARTUP_MODERN("Startup Modern", "Dynamic bold headers, versatile skill tags, fast-paced project showcases."),
    ONE_PAGE_IMPACT("One-Page Impact", "Laser-focused single-page summary engineered for high-volume recruiter triage.");

    private final String displayName;
    private final String description;

    ResumeTemplateType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
