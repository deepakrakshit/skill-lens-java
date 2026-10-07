package com.skilllens.service.rag;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Java-native deterministic embedding generator.
 * Produces 256-dimensional L2-normalized dense feature embeddings
 * utilizing subword tokenization, character n-grams, and Murmur/SHA feature projections.
 * Requires zero external dependencies or external model downloads.
 */
@Service
public class EmbeddingService {

    public static final int EMBEDDING_DIM = 256;
    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^a-zA-Z0-9_+#.-]+");

    /**
     * Generates a 256-dimensional unit-norm vector representation for any text chunk.
     */
    public float[] embed(String text) {
        float[] vector = new float[EMBEDDING_DIM];
        if (text == null || text.trim().isEmpty()) {
            return vector;
        }

        String cleaned = text.toLowerCase(Locale.ROOT).trim();
        String[] tokens = TOKEN_SPLIT.split(cleaned);

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            // 1. Whole word feature
            applyHash(token, vector, 1.0f);

            // 2. Character 3-grams and 4-grams for subword morphology
            if (token.length() >= 3) {
                for (int i = 0; i <= token.length() - 3; i++) {
                    String tri = token.substring(i, i + 3);
                    applyHash(tri, vector, 0.5f);
                }
            }
            if (token.length() >= 4) {
                for (int i = 0; i <= token.length() - 4; i++) {
                    String quad = token.substring(i, i + 4);
                    applyHash(quad, vector, 0.4f);
                }
            }
        }

        // L2 normalize
        float sumSquares = 0.0f;
        for (float v : vector) {
            sumSquares += v * v;
        }

        if (sumSquares > 0.0f) {
            float norm = (float) Math.sqrt(sumSquares);
            for (int i = 0; i < EMBEDDING_DIM; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    /**
     * Calculates cosine similarity between two unit-normalized vectors.
     */
    public double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1 == null || vec2 == null || vec1.length != vec2.length) {
            return 0.0;
        }
        double dotProduct = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
        }
        return Math.max(0.0, Math.min(1.0, dotProduct));
    }

    private void applyHash(String term, float[] vector, float weight) {
        int hash = term.hashCode();
        int index = Math.abs(hash % EMBEDDING_DIM);
        float sign = ((hash & 1) == 0) ? 1.0f : -1.0f;
        vector[index] += sign * weight;

        // Secondary hash for dispersion
        int secondaryHash = (hash * 31 + 17) ^ (hash >>> 16);
        int secondaryIndex = Math.abs(secondaryHash % EMBEDDING_DIM);
        vector[secondaryIndex] += (sign * 0.5f) * weight;
    }
}
