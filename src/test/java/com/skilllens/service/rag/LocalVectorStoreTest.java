package com.skilllens.service.rag;

import com.skilllens.domain.model.KnowledgeChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocalVectorStoreTest {

    private LocalVectorStore vectorStore;
    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        vectorStore = new LocalVectorStore();
        embeddingService = new EmbeddingService();
    }

    @Test
    @DisplayName("Enforces tenant isolation: student cannot access another student's evidence chunks")
    void testTenantIsolation() {
        // Global chunk
        KnowledgeChunk globalChunk = new KnowledgeChunk(
                "global-1", null, "Global Architecture Standards", "ROLE_REQUIREMENT",
                "Backend role requires database knowledge", embeddingService.embed("Backend role"), "Backend", "SQL"
        );

        // Student A chunk
        KnowledgeChunk studentAChunk = new KnowledgeChunk(
                "chunk-a-1", 101L, "Student A Resume", "RESUME_CLAIM",
                "Worked at SecretCorp on billing pipelines", embeddingService.embed("SecretCorp billing"), "Backend", "Java"
        );

        // Student B chunk
        KnowledgeChunk studentBChunk = new KnowledgeChunk(
                "chunk-b-1", 202L, "Student B Resume", "RESUME_CLAIM",
                "Student B confidential project", embeddingService.embed("Student B confidential"), "Backend", "Python"
        );

        vectorStore.indexChunks(List.of(globalChunk, studentAChunk, studentBChunk));

        // Query accessible chunks for Student A
        List<KnowledgeChunk> accessibleToA = vectorStore.getAccessibleChunks(101L);
        assertEquals(2, accessibleToA.size(), "Student A should only see global chunk and their own chunk");
        assertTrue(accessibleToA.stream().anyMatch(c -> c.chunkId().equals("global-1")));
        assertTrue(accessibleToA.stream().anyMatch(c -> c.chunkId().equals("chunk-a-1")));
        assertFalse(accessibleToA.stream().anyMatch(c -> c.chunkId().equals("chunk-b-1")), "Must NEVER leak Student B chunks to Student A");

        // Clear Student A chunks
        vectorStore.clearStudentChunks(101L);
        List<KnowledgeChunk> accessibleAfterClear = vectorStore.getAccessibleChunks(101L);
        assertEquals(1, accessibleAfterClear.size(), "Only global chunk should remain for Student A");
    }
}
