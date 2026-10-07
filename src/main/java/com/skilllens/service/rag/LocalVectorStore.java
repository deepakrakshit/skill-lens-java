package com.skilllens.service.rag;

import com.skilllens.domain.model.KnowledgeChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Local in-memory and embedded vector store with strict student context isolation.
 */
@Service
public class LocalVectorStore {

    private final Map<String, KnowledgeChunk> chunkStore = new ConcurrentHashMap<>();

    public void indexChunk(KnowledgeChunk chunk) {
        if (chunk != null && chunk.chunkId() != null) {
            chunkStore.put(chunk.chunkId(), chunk);
        }
    }

    public void indexChunks(List<KnowledgeChunk> chunks) {
        if (chunks != null) {
            for (KnowledgeChunk chunk : chunks) {
                indexChunk(chunk);
            }
        }
    }

    /**
     * Purges all evidence chunks associated with a specific student.
     */
    public void clearStudentChunks(Long studentId) {
        if (studentId == null) return;
        List<String> toRemove = chunkStore.values().stream()
                .filter(c -> studentId.equals(c.studentId()))
                .map(KnowledgeChunk::chunkId)
                .toList();
        toRemove.forEach(chunkStore::remove);
    }

    /**
     * Retrieves all chunks accessible to the current student.
     * Enforces strict tenant isolation: only returns global knowledge chunks
     * OR chunks specifically tagged with currentStudentId.
     */
    public List<KnowledgeChunk> getAccessibleChunks(Long currentStudentId) {
        return chunkStore.values().stream()
                .filter(c -> c.isGlobal() || (currentStudentId != null && currentStudentId.equals(c.studentId())))
                .collect(Collectors.toList());
    }

    public int size() {
        return chunkStore.size();
    }

    public void clearAll() {
        chunkStore.clear();
    }
}
