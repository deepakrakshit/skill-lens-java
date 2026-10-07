package com.skilllens.domain.model;

/**
 * 5-tier verification status model reflecting empirical evidence.
 *
 * Adheres strictly to the evidence-grounded non-accusatory standard:
 * - VERIFIED: Strong multi-signal evidence across repositories, language manifests, and recent commits.
 * - PARTIALLY_VERIFIED: Real repository presence but limited commits or indirect dependency evidence.
 * - WEAK_EVIDENCE: Superficial presence (e.g. fork without commits or isolated mention).
 * - CLAIMED_UNVERIFIED: Stated on resume, but no supporting public GitHub evidence discovered.
 * - MISSING: Required or preferred by the role, but absent from both resume and GitHub activity.
 */
public enum VerificationStatus {
    VERIFIED("Verified Evidence", "Multi-signal project and commit evidence confirms practical application."),
    PARTIALLY_VERIFIED("Partially Verified", "Repository presence confirmed, but evidence depth or commit recency is limited."),
    WEAK_EVIDENCE("Weak Evidence", "Minimal evidence found in public repositories or commits."),
    CLAIMED_UNVERIFIED("Claim Not Supported", "Skill claimed on resume, but not supported by available public GitHub evidence."),
    MISSING("Missing Requirement", "Skill is expected for target role, but not present in candidate profile or repositories.");

    private final String displayName;
    private final String defaultDescription;

    VerificationStatus(String displayName, String defaultDescription) {
        this.displayName = displayName;
        this.defaultDescription = defaultDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }
}
