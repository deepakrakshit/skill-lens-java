package com.skilllens.service.rag;

import com.skilllens.domain.model.KnowledgeChunk;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Hybrid search engine combining lexical keyword matching (BM25 approximation)
 * with dense cosine vector similarity, fused via Reciprocal Rank Fusion (RRF).
 */
@Service
public class HybridSearchService {

    private static final int RRF_K = 60;
    private static final Pattern WORD_SPLIT = Pattern.compile("[^a-zA-Z0-9_+#.-]+");

    private final LocalVectorStore vectorStore;
    private final EmbeddingService embeddingService;

    public HybridSearchService(LocalVectorStore vectorStore, EmbeddingService embeddingService) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
    }

    /**
     * Executes hybrid retrieval for a query scoped strictly to currentStudentId.
     *
     * @param query The user question or search phrase
     * @param currentStudentId Scoped student identifier
     * @param topK Number of results to return (typically 5 to 8)
     * @return Ordered list of most relevant KnowledgeChunk instances
     */
    public List<KnowledgeChunk> search(String query, Long currentStudentId, int topK) {
        List<KnowledgeChunk> accessibleChunks = vectorStore.getAccessibleChunks(currentStudentId);
        if (accessibleChunks.isEmpty() || query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        float[] queryVector = embeddingService.embed(query);
        Set<String> queryTerms = extractTerms(query);

        // 1. Lexical Scoring (BM25 term match)
        List<ScoredChunk> lexicalRanked = accessibleChunks.stream()
                .map(chunk -> new ScoredChunk(chunk, computeLexicalScore(chunk.content(), queryTerms)))
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .toList();

        // 2. Semantic Dense Scoring (Cosine similarity)
        List<ScoredChunk> denseRanked = accessibleChunks.stream()
                .map(chunk -> new ScoredChunk(chunk, embeddingService.cosineSimilarity(queryVector, chunk.vector())))
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .toList();

        // 3. Reciprocal Rank Fusion (RRF)
        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, KnowledgeChunk> chunkMap = new HashMap<>();

        for (int rank = 0; rank < lexicalRanked.size(); rank++) {
            KnowledgeChunk chunk = lexicalRanked.get(rank).chunk();
            chunkMap.put(chunk.chunkId(), chunk);
            double rrfTerm = 1.0 / (RRF_K + rank + 1);
            rrfScores.merge(chunk.chunkId(), rrfTerm, Double::sum);
        }

        for (int rank = 0; rank < denseRanked.size(); rank++) {
            KnowledgeChunk chunk = denseRanked.get(rank).chunk();
            chunkMap.put(chunk.chunkId(), chunk);
            double rrfTerm = 1.0 / (RRF_K + rank + 1);
            rrfScores.merge(chunk.chunkId(), rrfTerm, Double::sum);
        }

        // Sort by RRF score descending
        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(Math.max(1, topK))
                .map(entry -> chunkMap.get(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private double computeLexicalScore(String content, Set<String> terms) {
        if (content == null || terms.isEmpty()) return 0.0;
        String lower = content.toLowerCase(Locale.ROOT);
        double matches = 0.0;
        for (String term : terms) {
            if (lower.contains(term)) {
                matches += 1.0;
            }
        }
        return matches;
    }

    private Set<String> extractTerms(String text) {
        String[] tokens = WORD_SPLIT.split(text.toLowerCase(Locale.ROOT));
        Set<String> set = new HashSet<>();
        for (String t : tokens) {
            if (t.length() >= 2) {
                set.add(t);
            }
        }
        return set;
    }

    private record ScoredChunk(KnowledgeChunk chunk, double score) {}
}
