---
name: skill-lens-ai-evaluation
description: >-
  Defines automated evaluation frameworks, test datasets, and verification metrics for LLM outputs,
  structured extraction, hallucination detection, RAG retrieval quality, and resume rewriting integrity.
  Use when creating evaluation datasets, measuring precision/recall, testing mentor grounding, or benchmarking models.
---

# SkillLens AI Evaluation

This skill provides the quantitative evaluation framework, benchmark datasets, and metric definitions required to verify that AI components in SkillLens remain grounded, hallucination-resistant, and strictly factual.

---

## 1. Core Evaluation Principles

- **No Unmeasured Claims:** Never report arbitrary accuracy percentages without reproducible evaluation runs against verified benchmark datasets.
- **Deterministic Assertion First:** Validate schemas, JSON formats, non-empty outputs, and strict regex invariants deterministically before qualitative evaluation.
- **Zero Hallucination Target:** For resume rewriting and skill verification, the target rate for unsupported claims is 0.0%.

---

## 2. Evaluation Datasets & Benchmarks

The project maintains seven distinct evaluation datasets:
1. **Resume Skill Extraction Benchmark:** Annotated resume texts with ground-truth lists of explicit and implicit skills.
2. **Skill Normalization & Taxonomy Benchmark:** Alias test pairs (e.g. `Vue3`, `Vue.js 3`, `vue-router` → canonical `Vue.js`) with ground-truth categories.
3. **Buzzword & Exaggeration Benchmark:** Test cases featuring overstated claims (e.g. *"Expert in Kubernetes"* paired with zero container commits) to verify that the system correctly assigns unverified/unsupported status.
4. **Evidence Grounding Benchmark:** Paired resume claims and synthetic GitHub trees to measure verification status accuracy.
5. **Roadmap Coherence Benchmark:** Gap inputs paired with expected action sequencing, milestone relevance, and prerequisites.
6. **Resume Rewrite Integrity Benchmark:** Before/after resume pairs evaluated for unsupported fact introduction.
7. **Mentor Grounding Benchmark:** Query-context-response triplets evaluated for adherence to retrieved facts.

---

## 3. Quantitative Evaluation Metrics

### A. Extraction & Classification Metrics:
- **Precision ($P$):** Fraction of extracted skills that are truly valid candidate skills.
- **Recall ($R$):** Fraction of actual ground-truth skills successfully extracted.
- **F1 Score:** Harmonic mean: $F_1 = 2 \cdot \frac{P \cdot R}{P + R}$.

### B. Grounding & Safety Metrics:
- **Groundedness Score ($G$):** Percentage of generated assertions that can be mapped to an explicit statement in the retrieved candidate profile or GitHub evidence.
  $$G = \frac{\text{Directly Supported Claims}}{\text{Total Claims Made}} \times 100\%$$
- **Unsupported-Claim Rate ($UCR$):** Frequency at which generated resume rewrites or mentor suggestions introduce unverified technologies, dates, or employers.
  $$UCR = \frac{\text{Fabricated Entities}}{\text{Total Entities in Output}} \times 100\% \quad (\text{Target: } 0.0\%)$$

### C. RAG Retrieval Metrics:
- **Precision at $k$ ($P@k$):** Fraction of top-$k$ retrieved chunks that are genuinely relevant to the query.
- **Mean Reciprocal Rank ($MRR$):** Speed at which the first relevant document is ranked.

---

## 4. Automated Evaluation Pipeline

Evaluations are executable via automated JUnit / Spring Boot test fixtures:
- `mvn test -Dtest=*EvaluationTest`
- Output test results into structured evaluation summaries reporting exact sample counts, passed checks, and calculated $F_1$ / groundedness percentages.
