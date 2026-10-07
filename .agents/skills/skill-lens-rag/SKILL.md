---
name: skill-lens-rag
description: >-
  Establishes the local, Java-native retrieval-augmented generation (RAG) architecture for mentorship and career intelligence.
  Governs text chunking, embedding generation, hybrid search, reranking, source attribution, and contextual isolation.
  Use when indexing knowledge bases, retrieving candidate evidence, assembling mentor prompts, or evaluating retrieval quality.
---

# SkillLens RAG Architecture

This skill defines the technical implementation of the embedded, Java-native Retrieval-Augmented Generation (RAG) system powering AI mentorship and career intelligence.

---

## 1. Java-Native Runtime Standards

- **Zero Python Runtime:** All indexing, tokenization, vector computation, and retrieval are executed natively in Java 25.
- **Embedded Persistence:** Knowledge chunks, vector representations, and inverted indices are stored in local SQLite / H2 storage.
- **No External Vector DB Required:** Eliminates external service dependencies by running in-memory or embedded vector dot-product/cosine calculations.

---

## 2. Chunking & Indexing Pipeline

### Document Ingestion & Chunking:
- **Curated Knowledge Base:** Technical competencies, role requirements, interview rubrics, learning pathways.
- **Chunking Strategy:**
  - Semantic section-aware chunking (preserves Markdown headings, bulleted lists, and code blocks).
  - Target chunk size: 300 to 500 tokens with 50-token sliding overlap.
  - Chunk metadata: `chunk_id`, `document_id`, `category`, `role_tag`, `skill_tag`, `source_title`.

### Vector Embeddings:
- **Embedding Generation:**
  - Local Java-native vectorizer (TF-IDF / BM25 term-frequency vectors combined with pre-trained compact dense embeddings or Spring AI local embedding models).
  - Vectors normalized to unit length for cosine similarity.

---

## 3. Hybrid Search & Reranking

RAG retrieval combines lexical precision with semantic generalization:
1. **Lexical Retrieval (BM25):**
   - Matches exact skill keywords, tool names, and programming acronyms (e.g. `Kubernetes`, `JWT`, `B-Tree`).
2. **Semantic Retrieval (Cosine Similarity):**
   - Identifies conceptual equivalence (e.g. *"asynchronous task execution"* matches *"JavaFX concurrency Service"*).
3. **Score Combination (Reciprocal Rank Fusion):**
   - Combine ranks: $RRF(d) = \frac{1}{60 + r_{bm25}(d)} + \frac{1}{60 + r_{dense}(d)}$.
4. **Relevance Thresholding & Reranking:**
   - Discard chunks scoring below minimum confidence (0.45 threshold).
   - Re-rank top-k candidate chunks (top 5 to 8 chunks) by freshness and exact role alignment.

---

## 4. Student Context Isolation & Privacy

A critical privacy invariant:
- **Strict Tenant Isolation:** Every candidate profile, parsed resume, GitHub commit history, and mentor session is partitioned by student identifier.
- **Context Boundaries:** The RAG retrieval pipeline applies mandatory student ID scoping:
  ```sql
  SELECT * FROM candidate_evidence_chunks 
  WHERE student_id = :currentStudentId AND similarity > :threshold
  ```
- **Zero Cross-Contamination:** A student's mentor query can NEVER retrieve chunks belonging to a different student profile under any circumstance.

---

## 5. Grounded Context Assembly & Refusal Policy

When passing retrieved evidence into LLM prompts:
- **Structured Citation Blocks:**
  ```markdown
  ### RETRIEVED EVIDENCE
  [Doc 1 - GitHub Commit Evidence]: 14 commits in repo 'ml-pipeline' implementing data transforms.
  [Doc 2 - Role Requirements]: Backend Developer requires experience in database connection pooling.
  ```
- **Mandatory Refusal Directive:**
  - The model prompt must explicitly instruct:
    > "Answer the user question relying strictly on the retrieved context above. If the retrieved evidence does not contain sufficient facts to answer the question, state: 'Based on your currently available resume and GitHub evidence, there is no recorded project activity for this competency.'"
  - Never fabricate unrecorded skills or work experiences.
