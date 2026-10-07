# SkillLens Skill Toolkit Operational Usage Guide

This guide establishes the concrete invocation protocols, trigger conditions, input/output contracts, script execution procedures, and orchestration patterns for all 50 active skills in the SkillLens engineering toolkit.

---

## 1. Master Invocation Protocol

SkillLens is reconstructed as a modular Java 25 desktop application using JavaFX 25, Spring Boot, and Maven.
Engineering workflows are governed by `skill-lens-java-rebuild` as the Master Orchestrator, which delegates to specialized skills across 16 discrete implementation phases.

```
+-----------------------------------------------------------------------+
|             skill-lens-java-rebuild (Master Orchestrator)             |
+-----------------------------------+-----------------------------------+
                                    |
     +------------------------------+------------------------------+
     |                              |                              |
+----+--------------------+   +-----+--------------------+   +-----+--------------------+
|  Domain Intelligence    |   |  Desktop & Platform      |   |  Quality & Verification  |
|  - document             |   |  - javafx-* (16 skills)  |   |  - qa-methodology        |
|  - github               |   |  - sota-jvm              |   |  - junit-5-skill         |
|  - evidence-analysis    |   |  - java-architect        |   |  - java-junit-test-gen   |
|  - ai-routing           |   |  - dr-jskill             |   |  - skill-lens-ai-eval    |
|  - rag / mentorship     |   |  - ui-ux-pro-max         |   |  - verification-meth.    |
|  - resume-engineering   |   |  - spring-data-jpa       |   |  - systematic-debugging  |
+-------------------------+   +--------------------------+   +--------------------------+
```

---

## 2. Functional Domains & Skill Assignments

### Domain 1: Codebase Archaeology & Legacy Asset Extraction
- **Assigned Skills:** `software-architecture-analysis`, `api-inference-from-files`
- **Trigger Conditions:** Initial analysis of the reference Python FastAPI and React codebase.
- **Input:** Reference files, route definitions, prompt templates, taxonomy catalogs.
- **Output:** Extracted role rubrics, JSON schemas, prompt instructions, and API contract specifications.
- **Preconditions:** Reference codebase is read-only; zero direct runtime dependencies allowed.

---

### Domain 2: Domain Modeling & Target Architecture
- **Assigned Skills:** `software-architecture`, `migration-engineering`, `skill-lens-data-privacy`
- **Trigger Conditions:** Defining package hierarchies, bounded contexts, service contracts, and data models.
- **Input:** Extracted contracts from Domain 1, product functional requirements.
- **Output:** Clean domain boundary definitions, entity models, DTO schemas, and privacy policies.
- **Preconditions:** Multi-tier verification statuses and zero-hardcoding invariants strictly codified.

---

### Domain 3: Java 25 & Spring Boot Monolith Foundation
- **Assigned Skills:** `sota-jvm`, `java-architect`, `dr-jskill`, `java-springboot`, `create-spring-boot-java-project`
- **Trigger Conditions:** Bootstrapping the root Maven project, configuring Spring Boot container, and establishing modern Java 25 language conventions.
- **Input:** Maven coordinates, Java 25 compiler flags, Spring starter dependencies.
- **Output:** Compiling Java 25 project, Spring Boot application context, virtual thread executor configuration.
- **Preconditions:** JDK 25 LTS verified in environment; preview flags configured where necessary.

---

### Domain 4: JavaFX Desktop Shell & Lifecycle Integration
- **Assigned Skills:** `javafx-project-starter`, `javafx-application-lifecycle`, `javafx-architecture-frameworks`, `javafx-desktop-shell-integration`, `javafx-real-world-application-patterns`
- **Trigger Conditions:** Wiring the desktop window container, application init/start/stop lifecycle, and Spring context integration.
- **Input:** JavaFX 25 platform dependencies, AtlantaFX styles, primary window configuration.
- **Output:** Responsive desktop window, seamless Spring application context handoff, tray icon support, graceful shutdown hook.
- **Preconditions:** UI thread must remain completely non-blocking; all I/O offloaded to virtual threads.

---

### Domain 5: Desktop UI Architecture & 25 Screens
- **Assigned Skills:** `javafx-ui-layout-navigation`, `javafx-fxml-controls-css`, `javafx-concurrency-services`, `javafx-properties-bindings`, `javafx-reactive-binding-state`, `javafx-forms-validation-preferences`, `javafx-data-visualization-dashboards`, `javafx-theming-icons-styling`, `javafx-ecosystem-controls-productivity`, `ui-ux-pro-max`
- **Trigger Conditions:** Implementing the 25 core desktop views, navigation coordinator, card grids, metric gauges, and form inputs.
- **Input:** FXML templates, AtlantaFX Dark/Light theme tokens, Feather/Ikonli icons.
- **Output:** 25 responsive desktop views with MVVM view models, observable property bindings, and smooth transitions.
- **Preconditions:** Zero UI freezing; long-running operations must bind to `javafx.concurrent.Task` with progress indicators.

---

### Domain 6: Document Intelligence & Resume Parsing
- **Assigned Skills:** `skill-lens-document-intelligence`, `javafx-file-workflows-search`
- **Trigger Conditions:** Student submits a resume via drag-and-drop file upload, file chooser dialog, or raw text input.
- **Input:** File formats: PDF, DOCX, TXT, or raw pasted UTF-8 string.
- **Output:** Validated plain-text payload, document metadata, section boundaries, and candidate contact information.
- **Preconditions:** Untrusted files sanitized against zip-bomb and macro attacks; encrypted files rejected with clear user error.

---

### Domain 7: Candidate Modeling & Resume Engineering
- **Assigned Skills:** `skill-lens-resume-engineering`
- **Trigger Conditions:** Extracting structured candidate profile from parsed document text; formatting improved resume bullets; generating PDF/DOCX exports.
- **Input:** Raw resume text, target role requirements, verified GitHub evidence.
- **Output:** Structured candidate profile, 10 ATS-friendly resume templates, and native exported documents.
- **Preconditions:** Non-fabrication rule strictly enforced; rewritten bullets must be grounded in verified project activity.

---

### Domain 8: GitHub Intelligence & Commit Harvesting
- **Assigned Skills:** `skill-lens-github-intelligence`, `api-sdk-generator`
- **Trigger Conditions:** Student enters a GitHub username or profile URL.
- **Input:** Username string or GitHub profile URL.
- **Output:** Canonical username, profile metadata, repository list, languages, READMEs, topics, and latest 30 commits per project.
- **Preconditions:** Strict rate-limit handling (403, 429, Retry-After); local SHA-256 caching of unchanged repositories.

---

### Domain 9: Evidence Analysis & Multi-Signal Verification
- **Assigned Skills:** `skill-lens-evidence-analysis`, `backend-engineering`
- **Trigger Conditions:** Evaluating candidate resume claims against collected GitHub evidence and target role requirements.
- **Input:** Resume skills, GitHub evidence graph, role requirements.
- **Output:** 5-tier skill verification statuses (`VERIFIED`, `PARTIALLY VERIFIED`, `WEAK EVIDENCE`, `CLAIMED / UNVERIFIED`, `MISSING`), deterministic Role Fit score (0-100), and buzzword/exaggeration flags.
- **Preconditions:** Scoring must be fully explainable and deterministic; zero opaque black-box scoring.

---

### Domain 10: Multi-Provider AI Routing & Fallback
- **Assigned Skills:** `skill-lens-ai-routing`, `spring-ai`
- **Trigger Conditions:** Calling LLM services for extraction refinement, mentorship generation, or resume rewriting.
- **Input:** System prompt, user context, structured JSON schema.
- **Output:** Validated JSON response object, latency metrics, provider execution logs.
- **Configuration & Defaults:**
  - **PRIMARY AI:** Provider = Google Gemini, Default model = `gemini-3.5-flash`
  - **VISION AI:** Provider = Google Gemini, Default model = `gemini-3.5-flash`
  - **FALLBACK AI:** Provider = Groq, Default model = `openai/gpt-oss-120b` (Do NOT use Llama 3.3 as default fallback)
  - **Model Selector:** Dynamically configurable model selection.
- **Preconditions:** Zero hardcoding of API keys. Required credentials: `GEMINI_API_KEY`, `GROQ_API_KEY` (optional: `GITHUB_TOKEN`, `YOUTUBE_API_KEY`). Packaged credentials stored via OS-backed DPAPI; zero `.env` for end-user credential store. Primary provider automatically fails over to Groq on timeout, rate limiting (429), or service error.

---

### Domain 11: Local RAG Pipeline & AI Mentorship Engine
- **Assigned Skills:** `skill-lens-rag`, `skill-lens-career-mentorship`
- **Trigger Conditions:** Student engages with the AI career mentor, asks for skill guidance, or requests roadmap clarification.
- **Input:** Student query, candidate verification facts, learning resource index, chat history.
- **Output:** Contextually grounded mentorship advice, verified evidence citations, and personalized roadmap adjustments.
- **Preconditions:** Strict refusal policy for questions outside career/skills; strict grounding to student evidence.

---

### Domain 12: Learning Resource Discovery & YouTube Integration
- **Assigned Skills:** `skill-lens-learning-resources`
- **Trigger Conditions:** Generating learning recommendations for identified skill gaps.
- **Input:** Skill gap items, role competencies, student learning priorities.
- **Output:** Ranked technical tutorials, official YouTube playlists/videos, authoritative documentation links.
- **Preconditions:** YouTube Data API quota conservation via local response caching; offline fallback catalogs.

---

### Domain 13: Local Persistence & OS-Backed Security
- **Assigned Skills:** `spring-data-jpa`, `jpa-query-reviewer`, `secure-software-engineering`, `skill-lens-data-privacy`
- **Trigger Conditions:** Initializing database schema, saving candidate assessments, storing chat history, and managing API keys.
- **Input:** User preferences, API credentials, assessment results.
- **Output:** Relational persistence in SQLite/H2; OS-backed secure credential vault (Windows DPAPI / Keychain).
- **Preconditions:** Zero API keys or secrets written to plain-text configuration files or SQLite database; zero .env usage.

---

### Domain 14: Quality Engineering, Testing & AI Evaluation
- **Assigned Skills:** `qa-methodology`, `junit-5-skill`, `java-junit-test-generator`, `skill-lens-ai-evaluation`, `verification-methodology`, `systematic-debugging`
- **Trigger Conditions:** Running continuous unit tests, integration test suites, and AI grounding benchmark evaluations.
- **Input:** Test fixtures, synthetic candidate resumes, mock GitHub payloads, AI evaluation datasets.
- **Output:** 100% test pass reports, code coverage metrics (JaCoCo >= 80%), precision/recall evaluation scores.
- **Preconditions:** Deterministic assertions; all tests reproducible without network access.

---

### Domain 15: Windows Desktop Packaging & Native Distribution
- **Assigned Skills:** `javafx-testing-packaging-distribution`, `maven-tools`
- **Trigger Conditions:** Creating self-contained Windows executable and native MSI/EXE installer packages.
- **Input:** Modular Java runtime, compiled application JARs, native Windows icons, AtlantaFX assets.
- **Output:** Standalone Windows desktop application bundled with native JDK 25 via `jlink` and `jpackage`.
- **Preconditions:** No external JRE/JDK required on target user machine; clean installation and launch.

---

### Domain 16: Technical Documentation & Architecture Records
- **Assigned Skills:** `technical-documentation`
- **Trigger Conditions:** Finalizing architectural documentation, user operational guides, data schemas, and API contracts.
- **Input:** Architectural decisions, entity schemas, UI component trees.
- **Output:** `ARCHITECTURE.md`, `DATA_SCHEMA.md`, `API_CONTRACT.md`, `README.md`.
- **Preconditions:** Documentation must be clear, actionable, and strictly accurate to the final codebase.

---

## 3. Bundled Executable Scripts Execution Guide

The toolkit provides 35 executable scripts across 5 specialized skills. Below is the operational execution reference:

### A. Rebuild Orchestrator Scripts (`skills/skill-lens-java-rebuild/scripts/`)
| Script | Invocation Command | Operational Purpose |
|---|---|---|
| `check-environment.py` | `python skills/skill-lens-java-rebuild/scripts/check-environment.py` | Validates Java 25, compiler, jpackage, jlink, Git, Node. |
| `verify-skill-tree.py` | `python skills/skill-lens-java-rebuild/scripts/verify-skill-tree.py` | Verifies manifest integrity and frontmatter format across all skills. |
| `skill-audit.py` | `python skills/skill-lens-java-rebuild/scripts/skill-audit.py` | Audits bundled scripts and scans for destructive commands. |
| `run-java-build-check.py` | `python skills/skill-lens-java-rebuild/scripts/run-java-build-check.py` | Audits build descriptors and executes build test suites. |
| `package-readiness-check.py` | `python skills/skill-lens-java-rebuild/scripts/package-readiness-check.py` | Audits jpackage, jlink, and Windows packaging readiness. |
| `dependency-check.py` | `python skills/skill-lens-java-rebuild/scripts/dependency-check.py` | Enforces target 2026 stack versions (Java 25, JavaFX 25, Spring Boot, Maven). |
| `secret-scan.py` | `python skills/skill-lens-java-rebuild/scripts/secret-scan.py` | Ensures zero hard-coded credentials or private keys in the repository. |
| `reference-boundary-check.py` | `python skills/skill-lens-java-rebuild/scripts/reference-boundary-check.py` | Enforces zero production imports from reference directories. |
| `migration-parity-check.py` | `python skills/skill-lens-java-rebuild/scripts/migration-parity-check.py` | Tracks 10 core capability domains against reference parity. |

### B. UI/UX Pro Max Scripts (`skills/ui-ux-pro-max/scripts/`)
| Script | Invocation Command | Operational Purpose |
|---|---|---|
| `search.py` | `python skills/ui-ux-pro-max/scripts/search.py "query"` | Queries design patterns, color palettes, and UI rules. |
| `design_system.py` | `python skills/ui-ux-pro-max/scripts/design_system.py` | Generates tokens, typographic scales, and component themes. |
| `core.py` | `python skills/ui-ux-pro-max/scripts/core.py` | Core styling engine and asset indexing. |
| `validate_data.py` | `python skills/ui-ux-pro-max/scripts/validate_data.py` | Validates design system schema and data consistency. |
| `test_core.py` | `python skills/ui-ux-pro-max/scripts/test_core.py` | Unit tests for UI/UX styling engine. |
| `test_design_system_mode.py` | `python skills/ui-ux-pro-max/scripts/test_design_system_mode.py` | Validates theme switching between Dark and Light modes. |

### C. QA Methodology Scripts (`skills/qa-methodology/scripts/`)
| Script | Invocation Command | Operational Purpose |
|---|---|---|
| `check-ac-testability.py` | `python skills/qa-methodology/scripts/check-ac-testability.py "spec-file"` | Audits acceptance criteria for testability and completeness. |
| `risk-prioritize.py` | `python skills/qa-methodology/scripts/risk-prioritize.py "feature-list"` | Ranks testing priority based on architectural risk and user impact. |
| `test_check_ac_testability.py` | `python skills/qa-methodology/scripts/test_check_ac_testability.py` | Validates acceptance criteria auditor. |
| `test_risk_prioritize.py` | `python skills/qa-methodology/scripts/test_risk_prioritize.py` | Validates risk scoring algorithms. |

### D. Backend Engineering Scripts (`skills/backend-engineering/scripts/`)
| Script | Invocation Command | Operational Purpose |
|---|---|---|
| `n1-query-spotter.py` | `python skills/backend-engineering/scripts/n1-query-spotter.py "query-log"` | Detects potential N+1 query patterns in database logs. |
| `test_n1_query_spotter.py` | `python skills/backend-engineering/scripts/test_n1_query_spotter.py` | Validates N+1 query spotter algorithms. |

### E. Dr-JSkill Scripts (`skills/dr-jskill/scripts/`)
| Script | Invocation Command | Operational Purpose |
|---|---|---|
| `create-project-latest.mjs` | `node skills/dr-jskill/scripts/create-project-latest.mjs` | Scaffolds Spring Boot project configurations and dependency baselines. |
| `create-basic-project.mjs` | `node skills/dr-jskill/scripts/create-basic-project.mjs` | Generates minimal Java backend application templates. |
| `create-fullstack-project.mjs` | `node skills/dr-jskill/scripts/create-fullstack-project.mjs` | Full-stack project generator (reference utility). |
| `create-web-project.mjs` | `node skills/dr-jskill/scripts/create-web-project.mjs` | Web application template generator. |
| `checkpoint-and-run.sh` | `bash skills/dr-jskill/scripts/checkpoint-and-run.sh` | Shell wrapper for automated builds and testing checkpoints. |
| `sync-versions-in-docs.mjs` | `node skills/dr-jskill/scripts/sync-versions-in-docs.mjs` | Aligns dependency version strings across documentation. |
| `sync-workshop-docs.mjs` | `node skills/dr-jskill/scripts/sync-workshop-docs.mjs` | Documentation synchronization script. |
| `sync-workshop-docs.test.mjs` | `node skills/dr-jskill/scripts/sync-workshop-docs.test.mjs` | Test runner for documentation sync. |
| `normalize-vue-frontend.mjs` | `node skills/dr-jskill/scripts/normalize-vue-frontend.mjs` | Frontend asset normalizer. |
| `config.js` | `node skills/dr-jskill/scripts/config.js` | Project generation configuration rules. |
| `client.js` | `node skills/dr-jskill/scripts/client.js` | Spring Initializr client handler. |
| `doc-links.js` | `node skills/dr-jskill/scripts/doc-links.js` | Spring Boot reference documentation links. |
| `sidebar.js` | `node skills/dr-jskill/scripts/sidebar.js` | Documentation navigation builder. |
| `lib/versions.mjs` | `node skills/dr-jskill/scripts/lib/versions.mjs` | Authoritative JVM dependency version resolver. |

---

## 4. End-to-End Orchestrated Student Lifecycle Workflow

The core value flow of SkillLens follows this orchestrated skill pipeline:

```
[Student Input]
├── Resume File (PDF / DOCX / TXT) ---> skill-lens-document-intelligence
├── GitHub Username / URL           ---> skill-lens-github-intelligence
└── Target Role / Optional JD       ---> software-architecture-analysis
                                                   |
                                                   v
[Extraction & Harvesting]
├── Plain-text Resume Content       ---> skill-lens-resume-engineering (CandidateProfile)
└── 30 Commits/Repo & Metadata      ---> skill-lens-github-intelligence (EvidenceGraph)
                                                   |
                                                   v
[Verification & Gap Analysis]
├── Multi-Signal Matrix (5 Tiers)   ---> skill-lens-evidence-analysis
├── Deterministic Role Fit (0-100)  ---> skill-lens-evidence-analysis
└── Buzzword / Exaggeration Flags   ---> skill-lens-evidence-analysis
                                                   |
                                                   v
[Actionable Outputs]
├── 4-Month Personalized Roadmap    ---> skill-lens-career-mentorship
├── Curated Tutorials & YouTube     ---> skill-lens-learning-resources
├── RAG Grounded AI Career Mentor   ---> skill-lens-rag + skill-lens-career-mentorship
└── Optimized ATS-Friendly Resume   ---> skill-lens-resume-engineering (10 Templates)
```
