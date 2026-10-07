package com.skilllens.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recruiter_batches")
public class RecruiterBatch {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "role_title", length = 128, nullable = false)
    private String roleTitle;

    @Lob
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Lob
    @Column(name = "candidates_json", columnDefinition = "TEXT")
    private String candidatesJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RecruiterBatch() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    public RecruiterBatch(String roleTitle, String jobDescription) {
        this();
        this.roleTitle = roleTitle;
        this.jobDescription = jobDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getCandidatesJson() {
        return candidatesJson;
    }

    public void setCandidatesJson(String candidatesJson) {
        this.candidatesJson = candidatesJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
