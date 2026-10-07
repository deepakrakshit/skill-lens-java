package com.skilllens.domain.model;

import java.util.List;

/**
 * Knowledge chunk for local RAG indexing and evidence retrieval.
 */
public record KnowledgeChunk(
        String chunkId,
        Long studentId,       // null or -1 for global knowledge base, positive for student-specific evidence
        String sourceTitle,
        String category,      // "GITHUB_COMMIT", "RESUME_CLAIM", "ROLE_REQUIREMENT", "ROADMAP_OBJECTIVE", "TECHNICAL_CURRICULUM"
        String content,
        float[] vector,
        String roleTag,
        String skillTag
) {
    public boolean isGlobal() {
        return studentId == null || studentId <= 0;
    }
}
