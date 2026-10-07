package com.skilllens.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "career_roadmaps")
public class CareerRoadmap {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "candidate_id", length = 36, nullable = false)
    private String candidateId;

    @Column(name = "target_role", nullable = false)
    private String targetRole;

    @Column(name = "duration_months")
    private int durationMonths;

    @Lob
    @Column(name = "roadmap_months_json", columnDefinition = "TEXT")
    private String roadmapMonthsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CareerRoadmap() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.durationMonths = 4;
    }

    public CareerRoadmap(String candidateId, String targetRole, int durationMonths, String roadmapMonthsJson) {
        this();
        this.candidateId = candidateId;
        this.targetRole = targetRole;
        this.durationMonths = durationMonths;
        this.roadmapMonthsJson = roadmapMonthsJson;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    public String getRoadmapMonthsJson() {
        return roadmapMonthsJson;
    }

    public void setRoadmapMonthsJson(String roadmapMonthsJson) {
        this.roadmapMonthsJson = roadmapMonthsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
