package com.skilllens.service.rag;

import com.skilllens.domain.model.KnowledgeChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HybridSearchServiceTest {

    private HybridSearchService searchService;
    private LocalVectorStore vectorStore;
    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        vectorStore = new LocalVectorStore();
        embeddingService = new EmbeddingService();
        searchService = new HybridSearchService(vectorStore, embeddingService);
    }

    @Test
    @DisplayName("Retrieves most relevant knowledge chunk using RRF hybrid search")
    void testHybridSearchRetrieval() {
        KnowledgeChunk c1 = new KnowledgeChunk(
                "chunk-1", 1L, "PostgreSQL Transactions", "ROLE_REQUIREMENT",
                "Deep coverage of database transaction isolation levels, row locks, and ACID properties.",
                embeddingService.embed("database transaction isolation levels ACID"), "Backend", "SQL"
        );
        KnowledgeChunk c2 = new KnowledgeChunk(
                "chunk-2", 1L, "Frontend CSS Grid", "RESUME_CLAIM",
                "Styling modern responsive user interfaces with CSS Grid, Flexbox, and Tailwind.",
                embeddingService.embed("CSS Grid Flexbox UI styling"), "Frontend", "CSS"
        );
        KnowledgeChunk c3 = new KnowledgeChunk(
                "chunk-3", 1L, "Docker Containerization", "ROADMAP_OBJECTIVE",
                "Packaging microservices into lightweight container images using multi-step Dockerfiles.",
                embeddingService.embed("Docker containers microservices"), "DevOps", "Docker"
        );

        vectorStore.indexChunks(List.of(c1, c2, c3));

        List<KnowledgeChunk> results = searchService.search("database transaction isolation", 1L, 2);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("chunk-1", results.getFirst().chunkId(), "Top result should be the transaction chunk");
    }
}
