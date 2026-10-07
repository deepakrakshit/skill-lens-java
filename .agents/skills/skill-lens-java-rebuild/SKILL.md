---
name: skill-lens-java-rebuild
description: >-
  Master orchestration skill for the end-to-end implementation of SkillLens as an evidence-based
  student skill verification, career intelligence, AI mentorship, and resume optimization desktop application
  built with Java 25, JavaFX 25, Spring Boot, and Maven.
---

# SkillLens Master Rebuild & Integration Orchestrator

This meta-skill orchestrates the complete implementation of SkillLens according to its actual product definition: an evidence-based student skill verification, career roadmap, AI mentorship, GitHub repository intelligence, and resume optimization platform.

---

## 1. Actual Product Paradigm

```
RESUME CLAIMS
      +
REAL GITHUB PROJECT EVIDENCE (Last 30 Commits Per Relevant Repo)
      +
TARGET ROLE REQUIREMENTS (or Optional Job Description Override)
      ↓
SKILL VERIFICATION (VERIFIED / PARTIAL / WEAK / UNVERIFIED / MISSING)
      ↓
SKILL GAP ANALYSIS (High / Medium / Low Priority Gaps)
      ↓
ROLE FIT (Deterministic 0-100 Score + Explainable Evidence)
      ↓
CAREER ROADMAP (Month 1, 2, 3, 4 Personalized Action Plan)
      ↓
AI MENTORSHIP (RAG Grounded in Verified Candidate Facts)
      ↓
RESUME OPTIMIZATION (Evidence-Grounded Rewrite + 10 ATS-Friendly Templates + PDF/DOCX Export)
```

---

## 2. Phase Execution Matrix & Specialist Delegation

The rebuild is structured across 16 cohesive engineering phases:

```
PHASE 1: Codebase Archaeology & Legacy Asset Extraction
├── Specialist Skills: software-architecture-analysis, api-inference-from-files
└── Verification: Extract role catalogs, prompt rubrics, and taxonomy files from reference code

PHASE 2: Domain Modeling & Target Architecture
├── Specialist Skills: software-architecture, migration-engineering, skill-lens-data-privacy
└── Verification: Define bounded contexts, entities, DTOs, and clean boundaries

PHASE 3: Java 25 & Spring Boot Monolith Foundation
├── Specialist Skills: sota-jvm, java-architect, dr-jskill
└── Verification: Maven build, JDK 25 runtime, Spring Boot container startup

PHASE 4: JavaFX Desktop Shell & Lifecycle Integration
├── Specialist Skills: javafx-architecture-frameworks, javafx-application-lifecycle, javafx-reactive-binding-state
└── Verification: Seamless Spring context handoff to JavaFX Application, non-blocking UI thread

PHASE 5: Desktop UI Architecture & Screen Navigation (25 Screens)
├── Specialist Skills: javafx-ui-layout-navigation, javafx-fxml-controls-css, javafx-theming-icons-styling, ui-ux-pro-max
└── Verification: 25 screens wired with AtlantaFX theme tokens and view switching

PHASE 6: Document Intelligence & Resume Parsing
├── Specialist Skills: skill-lens-document-intelligence, skill-lens-resume-engineering
└── Verification: Extract text/metadata from PDF, DOCX, TXT, and pasted text with Tika/PDFBox/POI

PHASE 7: GitHub Intelligence & Commit Harvesting
├── Specialist Skills: skill-lens-github-intelligence
└── Verification: Normalization, rate-limit resilience, repo discovery, last-30-commits collection

PHASE 8: Multi-Signal Evidence Verification & Deterministic Scoring
├── Specialist Skills: skill-lens-evidence-analysis
└── Verification: Multi-signal verification tiers, exaggeration detection, deterministic Role Fit calculation

PHASE 9: Multi-Provider AI Routing & Fallback (Gemini + Groq)
├── Specialist Skills: skill-lens-ai-routing
└── Verification: Primary Gemini (gemini-3.5-flash), Vision Gemini (gemini-3.5-flash), Groq fallback (openai/gpt-oss-120b), structured JSON outputs

PHASE 10: Local RAG Pipeline & AI Mentorship Engine
├── Specialist Skills: skill-lens-rag, skill-lens-career-mentorship
└── Verification: Hybrid search, student-scoped context retrieval, refusal policy, conversational persistence

PHASE 11: Learning Resource Discovery & YouTube Integration
├── Specialist Skills: skill-lens-learning-resources
└── Verification: Official YouTube Data API query caching, quota conservation, graceful offline fallback

PHASE 12: Resume Rewriting Engine & 10 ATS-Friendly Templates
├── Specialist Skills: skill-lens-resume-engineering
└── Verification: Non-fabrication rewrite rule, 10 ATS-friendly templates, native PDF/DOCX generation

PHASE 13: Local Persistence & OS-Backed Secure Credential Storage
├── Specialist Skills: spring-data-jpa, jpa-query-reviewer, skill-lens-data-privacy
└── Verification: SQLite/H2 schema migrations, Windows DPAPI / Keychain credential storage, zero keys in DB

PHASE 14: Automated Testing & AI Evaluation Benchmarks
├── Specialist Skills: qa-methodology, junit-5-skill, java-junit-test-generator, skill-lens-ai-evaluation
└── Verification: 100% test pass rate, precision/recall benchmarks, zero unsupported claims

PHASE 15: Windows Desktop Packaging & Native Distribution
├── Specialist Skills: javafx-testing-packaging-distribution, maven-tools
└── Verification: jpackage self-contained Windows desktop distribution without external runtime dependencies

PHASE 16: Project Documentation & Architecture Records
├── Specialist Skills: technical-documentation
└── Verification: Complete ARCHITECTURE.md, DATA_SCHEMA.md, API_CONTRACT.md, and runbooks
```

---

## 3. Local Deterministic Helper Scripts

All 9 deterministic helper scripts located in `skills/skill-lens-java-rebuild/scripts/` must be executed to verify gates:
1. `check-environment.py`: Validates Java 25, compiler, jpackage, jlink, Git, Node.
2. `verify-skill-tree.py`: Verifies manifest integrity and frontmatter format across all skills.
3. `skill-audit.py`: Catalogs scripts, references, and scans for dangerous system calls.
4. `run-java-build-check.py`: Audits build descriptors and executes build test suites.
5. `package-readiness-check.py`: Audits jpackage, jlink, and Windows packaging readiness.
6. `dependency-check.py`: Enforces target 2026 stack versions (Java 25, JavaFX 25, Spring Boot, Maven).
7. `secret-scan.py`: Ensures zero hard-coded credentials or private keys in the repository.
8. `reference-boundary-check.py`: Enforces zero production imports from reference directories.
9. `migration-parity-check.py`: Tracks 10 core capability domains against reference parity.
