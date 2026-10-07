package com.skilllens.service.rag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingServiceTest {

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService();
    }

    @Test
    @DisplayName("Generates 256-dimensional unit-length vectors")
    void testVectorDimensionsAndNormalization() {
        String text = "Spring Boot microservices architecture with PostgreSQL";
        float[] vector = embeddingService.embed(text);

        assertNotNull(vector);
        assertEquals(256, vector.length);

        // Verify unit norm (sum of squares is ~1.0)
        double sumSq = 0.0;
        for (float v : vector) {
            sumSq += v * v;
        }
        assertEquals(1.0, sumSq, 0.01, "Vector must be normalized to unit length");
    }

    @Test
    @DisplayName("Calculates higher cosine similarity for related texts than unrelated texts")
    void testCosineSimilaritySemantics() {
        float[] vJava1 = embeddingService.embed("Java Spring Boot backend service with REST APIs");
        float[] vJava2 = embeddingService.embed("Java Spring Framework backend engineering and APIs");
        float[] vCooking = embeddingService.embed("Baking chocolate chip cookies in an oven with sugar and flour");

        double simRelated = embeddingService.cosineSimilarity(vJava1, vJava2);
        double simUnrelated = embeddingService.cosineSimilarity(vJava1, vCooking);

        assertTrue(simRelated > simUnrelated, "Related technical terms must have higher similarity than unrelated topics");
        assertEquals(1.0, embeddingService.cosineSimilarity(vJava1, vJava1), 0.001, "Self similarity must be 1.0");
    }
}
