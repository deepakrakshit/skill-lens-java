# SkillLens Active Skills Map

This reference maps all 50 installed and validated skills to their operational purpose, activation triggers, rebuild project phase, and associated scripts or reference materials.

---

## Skill Activation Reference Model

`
Skill
  ├── Purpose
  ├── When to activate
  ├── Relevant project phase
  └── Scripts / resources to use
`

---

### 1. software-architecture-analysis
- **Purpose:** Systematic codebase archaeology, contract extraction, dependency mapping, and reverse engineering.
- **When to activate:** Before modifying or migrating code; when analyzing the Python/React reference codebase.
- **Relevant project phase:** Phase 1 (Codebase Archaeology & Legacy Asset Extraction).
- **Scripts / resources to use:** 
eferences/ guides on architectural mapping and interface contract extraction.

---

### 2. migration-engineering
- **Purpose:** Governs multi-phase migration lifecycles, dual-run verification, parity tracking, and risk mitigation.
- **When to activate:** Designing the migration path from Python + React to Java 25 + JavaFX; verifying parity across features.
- **Relevant project phase:** Phase 2 (Domain Modeling & Target Architecture) & continuous verification across all phases.
- **Scripts / resources to use:** 
eferences/ guides on migration patterns; local scripts/migration-parity-check.py.

---

### 3. software-architecture
- **Purpose:** System decomposition, modular component modeling, and architectural boundary definition.
- **When to activate:** Designing package hierarchies, interface boundaries, and data flow pipelines.
- **Relevant project phase:** Phase 2 (Domain Modeling & Target Architecture).
- **Scripts / resources to use:** Architectural pattern templates and component interaction models.

---

### 4. sota-jvm
- **Purpose:** Modern JVM engineering standards, Java 25 language features (virtual threads, pattern matching, records).
- **When to activate:** Implementing core Java classes, records, concurrency constructs, and modern idioms.
- **Relevant project phase:** Phase 3 (Java 25 & Spring Boot Monolith Foundation) & ongoing backend engineering.
- **Scripts / resources to use:** 
ules/01-idioms.md, 
ules/03-concurrency.md, 
ules/05-performance.md.

---

### 5. java-architect
- **Purpose:** Enterprise Java architecture specialist: Clean Architecture, modular boundaries, JPA performance.
- **When to activate:** Designing service contracts, application module boundaries, and transaction managers.
- **Relevant project phase:** Phase 3 (Java 25 & Spring Boot Foundation) & Phase 13 (Local Persistence).
- **Scripts / resources to use:** 
eferences/jpa-optimization.md, 
eferences/spring-boot-setup.md.

---

### 6. dr-jskill
- **Purpose:** Executable Java 25 & Spring Boot 4.x project generator, best practice rules, and toolchain presets.
- **When to activate:** Bootstrapping project dependencies, Docker assets, versions, and application structure.
- **Relevant project phase:** Phase 3 (Java 25 & Spring Boot Foundation).
- **Scripts / resources to use:** scripts/create-project-latest.mjs, scripts/lib/versions.mjs, 
eferences/SPRING-BOOT-4.md.

---

### 7. java-springboot
- **Purpose:** Spring Boot conventions, dependency injection patterns, service lifecycle, and configuration properties.
- **When to activate:** Wiring Spring Boot application context, component scanning, and application properties.
- **Relevant project phase:** Phase 3 (Java 25 & Spring Boot Foundation).
- **Scripts / resources to use:** Spring Boot configuration conventions and injection guides.

---

### 8. create-spring-boot-java-project
- **Purpose:** Spring Initializr API dependency selection and project bootstrapping workflows.
- **When to activate:** Selecting Spring starter dependencies and validating starter compatibility.
- **Relevant project phase:** Phase 3 (Java 25 & Spring Boot Foundation).
- **Scripts / resources to use:** Dependency starter mapping guides.

---

### 9. javafx-project-starter
- **Purpose:** Initialize modular JavaFX application with OpenJFX 25, build plugins, and module descriptors.
- **When to activate:** Creating JavaFX root application class, configuring module-info, and setting up build scripts.
- **Relevant project phase:** Phase 4 (JavaFX Desktop Shell & Lifecycle Integration).
- **Scripts / resources to use:** JavaFX modular project bootstrap templates.

---

### 10. javafx-application-lifecycle
- **Purpose:** Govern JavaFX application lifecycle init/start/stop, thread handoffs, and graceful shutdown.
- **When to activate:** Wiring Spring context initialization into JavaFX Application.init(), handling window close events.
- **Relevant project phase:** Phase 4 (JavaFX Desktop Shell & Lifecycle Integration).
- **Scripts / resources to use:** JavaFX Application lifecycle patterns, Spring shutdown hooks.

---

### 11. javafx-architecture-frameworks
- **Purpose:** Structure JavaFX UI with MVVM, MVC, dependency injection, and clean state boundaries.
- **When to activate:** Designing view models, controllers, and state management patterns.
- **Relevant project phase:** Phase 4 (Desktop Shell) & Phase 5 (Desktop UI Architecture & 25 Screens).
- **Scripts / resources to use:** JavaFX MVVM architecture blueprints.

---

### 12. javafx-ui-layout-navigation
- **Purpose:** Design responsive JavaFX layouts, pane hierarchies, and view navigation transitions.
- **When to activate:** Implementing screen layout containers, sidebar navigation, and card grids.
- **Relevant project phase:** Phase 5 (Desktop UI Architecture & Screen Navigation).
- **Scripts / resources to use:** Navigation pattern guides, screen swapping coordinator patterns.

---

### 13. javafx-fxml-controls-css
- **Purpose:** Declarative FXML UI definition, controller wiring, and CSS styling.
- **When to activate:** Creating FXML views, binding controller fields, and applying modular CSS rules.
- **Relevant project phase:** Phase 5 (Desktop UI Architecture & Screen Navigation).
- **Scripts / resources to use:** FXML templates and controller binding blueprints.

---

### 14. javafx-concurrency-services
- **Purpose:** Non-blocking background computation using JavaFX Task, Service, and Virtual Threads.
- **When to activate:** Offloading PDF parsing, GitHub API calls, and LLM inference to background threads.
- **Relevant project phase:** Phase 4, Phase 6, Phase 7, Phase 9, Phase 10.
- **Scripts / resources to use:** Background Task patterns, UI progress binding utilities.

---

### 15. javafx-properties-bindings
- **Purpose:** Observable properties, uni/bidirectional data bindings, and change listeners.
- **When to activate:** Synchronizing view models with UI controls (progress bars, status labels, form inputs).
- **Relevant project phase:** Phase 5 (Desktop UI Architecture) & Phase 10 (Mentorship Chat UI).
- **Scripts / resources to use:** Property binding patterns, observable collections.

---

### 16. javafx-reactive-binding-state
- **Purpose:** Advanced reactive state management, event streams, state machines, and debounced events.
- **When to activate:** Implementing complex state transitions (multi-step verification, real-time filtering).
- **Relevant project phase:** Phase 4 (Desktop Shell) & Phase 8 (Evidence Verification).
- **Scripts / resources to use:** Reactive state patterns, event debounce utilities.

---

### 17. javafx-forms-validation-preferences
- **Purpose:** Desktop form inputs, field validation, error indicators, and user preference persistence.
- **When to activate:** Building resume upload form, GitHub username input, role selector, and settings dialogs.
- **Relevant project phase:** Phase 5 (Desktop UI) & Phase 13 (Local Persistence & Preferences).
- **Scripts / resources to use:** Form validation blueprints, preferences binding patterns.

---

### 18. javafx-file-workflows-search
- **Purpose:** Native FileChooser dialogs, drag-and-drop file ingestion, directory watching, and file search.
- **When to activate:** Implementing resume file drag-and-drop and native file browsing.
- **Relevant project phase:** Phase 6 (Document Intelligence & Resume Parsing).
- **Scripts / resources to use:** Drag-and-drop file handlers, FileChooser helpers.

---

### 19. javafx-data-visualization-dashboards
- **Purpose:** Charts, gauges, metric cards, and visualization dashboards in JavaFX.
- **When to activate:** Building Role Fit meters, Skill Radar charts, Gap breakdown graphs, and confidence gauges.
- **Relevant project phase:** Phase 5 (Desktop UI Architecture) & Phase 8 (Evidence Verification).
- **Scripts / resources to use:** JavaFX Chart customization guides, dashboard layout patterns.

---

### 20. javafx-theming-icons-styling
- **Purpose:** AtlantaFX dark/light themes, Feather/Ikonli icon integration, and modern desktop UI aesthetics.
- **When to activate:** Styling the application UI, dark/light theme switching, and icon selection.
- **Relevant project phase:** Phase 5 (Desktop UI Architecture & 25 Screens).
- **Scripts / resources to use:** AtlantaFX theme integration guides, CSS variables.

---

### 21. ui-ux-pro-max
- **Purpose:** Professional UI/UX design intelligence, design systems, palette generator, and JavaFX stack guidelines.
- **When to activate:** Designing layout grids, color tokens, typography scales, and micro-interactions.
- **Relevant project phase:** Phase 5 (Desktop UI Architecture & 25 Screens).
- **Scripts / resources to use:** scripts/search.py, scripts/design_system.py, scripts/core.py.

---

### 22. spring-ai
- **Purpose:** Spring AI ChatClient, structured output converters, prompt templating, and RAG advisors.
- **When to activate:** Integrating Gemini/Groq LLM providers, constructing prompt templates, and structuring JSON responses.
- **Relevant project phase:** Phase 9 (Multi-Provider AI Routing) & Phase 10 (Local RAG Pipeline).
- **Scripts / resources to use:** ChatClient configuration patterns, PromptTemplate blueprints.

---

### 23. spring-data-jpa
- **Purpose:** Spring Data JPA repositories, entity lifecycle, embedded database configuration (SQLite / H2).
- **When to activate:** Defining candidate entities, verification records, chat session history, and repository queries.
- **Relevant project phase:** Phase 13 (Local Persistence & OS-Backed Secure Credential Storage).
- **Scripts / resources to use:** Entity mapping guides, repository query methods.

---

### 24. jpa-query-reviewer
- **Purpose:** JPA query optimization, N+1 detection, fetch plan tuning, and persistence context management.
- **When to activate:** Auditing repository queries, tuning relationship fetching, and preventing query bottlenecks.
- **Relevant project phase:** Phase 13 (Local Persistence & OS-Backed Storage).
- **Scripts / resources to use:** Query optimization checklists, N+1 detection guides.

---

### 25. ackend-engineering
- **Purpose:** Robust backend service design, transaction boundaries, idempotency, and error handling.
- **When to activate:** Implementing application services, boundary facades, and business rules.
- **Relevant project phase:** Phase 6 through Phase 13.
- **Scripts / resources to use:** scripts/n1-query-spotter.py, service pattern guides.

---

### 26. pi-inference-from-files
- **Purpose:** Extract endpoint definitions, request/response models, and contract schemas from source files.
- **When to activate:** Reverse-engineering API routes from the reference Python FastAPI backend.
- **Relevant project phase:** Phase 1 (Codebase Archaeology & Legacy Asset Extraction).
- **Scripts / resources to use:** Endpoint extraction heuristics, contract mapping patterns.

---

### 27. pi-sdk-generator
- **Purpose:** Generate strongly-typed Java client models, DTOs, and HTTP wrappers from API specifications.
- **When to activate:** Constructing typed DTOs for GitHub API and external service integrations.
- **Relevant project phase:** Phase 7 (GitHub Intelligence) & Phase 11 (Learning Resources).
- **Scripts / resources to use:** DTO generator patterns, HTTP client templates.

---

### 28. qa-methodology
- **Purpose:** QA strategy, test design techniques, risk-based prioritization, and quality gates.
- **When to activate:** Defining test suites, test matrices, acceptance criteria, and CI verification gates.
- **Relevant project phase:** Phase 14 (Automated Testing & AI Evaluation Benchmarks).
- **Scripts / resources to use:** scripts/check-ac-testability.py, scripts/risk-prioritize.py.

---

### 29. junit-5-skill
- **Purpose:** Modern JUnit 5 parameterized tests, nested test architectures, and assertion suites.
- **When to activate:** Writing unit and integration test classes for services, parsers, and repositories.
- **Relevant project phase:** Phase 14 (Automated Testing & AI Evaluation Benchmarks).
- **Scripts / resources to use:** 
eference/playbook.md, 
eference/advanced-patterns.md.

---

### 30. java-junit-test-generator
- **Purpose:** Structured JUnit & Mockito test generation workflows and coverage templates.
- **When to activate:** Generating Mockito test harnesses for external adapters and services.
- **Relevant project phase:** Phase 14 (Automated Testing & AI Evaluation Benchmarks).
- **Scripts / resources to use:** 
eferences/mockito-patterns-guide.md, 	emplates/test-class-template.md.

---

### 31. erification-methodology
- **Purpose:** Rigorous acceptance testing, verification gates, proof collection, and regression defense.
- **When to activate:** Running acceptance verification prior to declaring any phase complete.
- **Relevant project phase:** Continuous verification across all 16 project phases.
- **Scripts / resources to use:** Verification gate checklists, evidence logging templates.

---

### 32. systematic-debugging
- **Purpose:** Scientific root-cause analysis, hypothesis generation, and deterministic failure isolation.
- **When to activate:** Diagnosing unexpected bugs, UI rendering issues, or service failures.
- **Relevant project phase:** All phases upon test or runtime failure.
- **Scripts / resources to use:** Systematic hypothesis testing playbook.

---

### 33. secure-software-engineering
- **Purpose:** Threat modeling, secrets management, input sanitization, and desktop security hardening.
- **When to activate:** Designing credential management, handling untrusted PDF resumes, reviewing external calls.
- **Relevant project phase:** Phase 13 (Local Persistence & OS-Backed Storage).
- **Scripts / resources to use:** Threat modeling checklists, local scripts/secret-scan.py.

---

### 34. maven-tools
- **Purpose:** Live Maven Central dependency verification, version resolution, and artifact availability.
- **When to activate:** Adding or upgrading dependencies in Maven/Gradle build scripts.
- **Relevant project phase:** Phase 3 (Foundation) & Phase 15 (Windows Desktop Packaging).
- **Scripts / resources to use:** Maven Central search and version verification workflows.

---

### 35. javafx-desktop-shell-integration
- **Purpose:** Desktop window containers, system tray integration, dialogs, and OS notifications.
- **When to activate:** Configuring window decorations, system tray icon, minimize-to-tray, and OS dialogs.
- **Relevant project phase:** Phase 4 (Desktop Shell) & Phase 15 (Packaging & Distribution).
- **Scripts / resources to use:** Desktop shell templates, system tray handlers, dialog helpers.

---

### 36. javafx-ecosystem-controls-productivity
- **Purpose:** Rich JavaFX ecosystem controls: AtlantaFX, ControlsFX, Ikonli, FX-Gson.
- **When to activate:** Integrating high-productivity UI controls (autocomplete fields, validation overlays, badges).
- **Relevant project phase:** Phase 5 (Desktop UI Architecture & 25 Screens).
- **Scripts / resources to use:** ControlsFX dialog helpers, Ikonli font-icon catalogs.

---

### 37. javafx-real-world-application-patterns
- **Purpose:** Composite desktop application blueprints, error boundaries, and production architectural patterns.
- **When to activate:** Reviewing end-to-end desktop ergonomics, uncaught exception handlers, and performance.
- **Relevant project phase:** Phase 4 (Desktop Shell) & Phase 15 (Packaging & Distribution).
- **Scripts / resources to use:** Global exception handlers, modular application blueprints.

---

### 38. javafx-testing-packaging-distribution
- **Purpose:** TestFX headless UI testing, modular jlink packaging, and jpackage installer creation.
- **When to activate:** Configuring distribution tasks, jlink modular image, and Windows installer generation.
- **Relevant project phase:** Phase 15 (Windows Desktop Packaging & Native Distribution).
- **Scripts / resources to use:** jlink and jpackage configuration patterns; local scripts/package-readiness-check.py.

---

### 39. 	echnical-documentation
- **Purpose:** Engineering documentation, architecture decision records (ADRs), user guides, runbooks.
- **When to activate:** Authoring README, ARCHITECTURE, and user-facing operational manuals.
- **Relevant project phase:** Phase 16 (Project Documentation & Architecture Records).
- **Scripts / resources to use:** Documentation templates, ADR blueprints.

---

### 40. skill-lens-java-rebuild (Master Orchestrator)
- **Purpose:** Project-owned master orchestrator governing rebuild lifecycle, delegation paths, and verification gates.
- **When to activate:** Always active across the entire rebuild initiative.
- **Relevant project phase:** All 16 project phases.
- **Scripts / resources to use:** All 9 deterministic scripts in skills/skill-lens-java-rebuild/scripts/.

---

### 41. skill-lens-github-intelligence
- **Purpose:** GitHub REST API interaction, canonical username normalization, rate-limit resilience, public repo discovery, latest 30 commits extraction, evidence graph building, and local caching.
- **When to activate:** Ingesting student GitHub profiles, discovering repositories, harvesting commits, and isolating GitHub API boundaries.
- **Relevant project phase:** Phase 7 (GitHub Intelligence & Commit Harvesting).
- **Scripts / resources to use:** Rate-limit handlers, pagination routines, SHA-256 cache stores.

---

### 42. skill-lens-document-intelligence
- **Purpose:** Robust Java-native document ingestion, extraction, and validation for resumes across PDF, DOCX, TXT, and plain-text formats using Apache Tika, PDFBox, and POI.
- **When to activate:** Parsing uploaded resumes, validating document security, handling corrupted/encrypted files, and extracting structured candidate sections.
- **Relevant project phase:** Phase 6 (Document Intelligence & Resume Parsing).
- **Scripts / resources to use:** Apache Tika metadata extractors, PDFBox text strippers, POI docx extractors.

---

### 43. skill-lens-resume-engineering
- **Purpose:** Structured candidate profile modeling, skill alias normalization, evidence-grounded resume rewriting, ATS-friendly layout design across 10 professional templates, and Java-native PDF/DOCX generation.
- **When to activate:** Normalizing candidate data, calculating resume scores, rewriting bullets with XYZ impact framing, and exporting documents.
- **Relevant project phase:** Phase 6 (Resume Extraction) & Phase 12 (Resume Rewriting & 10 ATS Templates).
- **Scripts / resources to use:** OpenPDF layout engines, Apache POI DOCX templates, 10 ATS template stylesheets.

---

### 44. skill-lens-rag
- **Purpose:** Local Java-native retrieval-augmented generation (RAG) architecture for mentorship and career intelligence: chunking, embeddings, hybrid search, reranking, source attribution, and contextual isolation.
- **When to activate:** Indexing learning resources, retrieving candidate evidence, constructing mentorship prompt context, and evaluating retrieval relevance.
- **Relevant project phase:** Phase 10 (Local RAG Pipeline & AI Mentorship Engine).
- **Scripts / resources to use:** Java-native embedding models, SQLite vector/FTS5 indexes, reciprocal rank fusion.

---

### 45. skill-lens-ai-evaluation
- **Purpose:** Automated evaluation frameworks, test datasets, and verification metrics for LLM outputs, structured extraction, hallucination detection, RAG retrieval quality, and resume rewriting integrity.
- **When to activate:** Benchmarking AI output quality, evaluating mentor grounding, measuring precision/recall on skill extraction, and testing non-fabrication compliance.
- **Relevant project phase:** Phase 14 (Automated Testing & AI Evaluation Benchmarks).
- **Scripts / resources to use:** Grounding evaluators, synthetic test datasets, deterministic assertion suites.

---

### 46. `skill-lens-ai-routing`
- **Purpose:** Multi-provider LLM routing: Primary/Vision AI with Google Gemini (`gemini-3.5-flash`), Fallback AI with Groq (`openai/gpt-oss-120b`), dynamic model selector, credential management, structured output generation, and AI observability.
- **When to activate:** Invoking LLM capabilities, handling API quota exhaustion, switching between primary and fallback providers, configuring credentials, and parsing structured JSON.
- **Relevant project phase:** Phase 9 (Multi-Provider AI Routing & Fallback).
- **Scripts / resources to use:** Multi-provider failover coordinators, JSON schema enforcers, latency metric recorders.

---

### 47. skill-lens-evidence-analysis
- **Purpose:** Multi-signal skill verification model, deterministic confidence scoring, buzzword and exaggeration detection, and evidence graph construction.
- **When to activate:** Comparing resume claims against GitHub activity, assigning 5-tier verification statuses, and calculating explainable Role Fit scores.
- **Relevant project phase:** Phase 8 (Multi-Signal Evidence Verification & Deterministic Scoring).
- **Scripts / resources to use:** 5-tier verification matrix, deterministic scoring algorithms, exaggeration detectors.

---

### 48. skill-lens-career-mentorship
- **Purpose:** Personalized month-by-month career roadmap generation, competency mapping, RAG-grounded AI mentorship conversations, context assembly, and progress tracking.
- **When to activate:** Generating customized 4-month learning roadmaps, managing conversational career mentorship sessions, and enforcing mentor refusal policies.
- **Relevant project phase:** Phase 10 (Local RAG Pipeline & AI Mentorship Engine).
- **Scripts / resources to use:** 4-month roadmap generators, student-scoped chat memory managers, mentorship prompt rubrics.

---

### 49. skill-lens-learning-resources
- **Purpose:** Learning resource discovery, YouTube Data API integration, curriculum ranking, caching, quota management, and graceful offline fallback.
- **When to activate:** Finding high-quality technical tutorials, retrieving YouTube playlists, recommending authoritative documentation, and managing API quotas.
- **Relevant project phase:** Phase 11 (Learning Resource Discovery & YouTube Integration).
- **Scripts / resources to use:** YouTube API clients, quota-conserving caching layers, curated offline fallback catalogs.

---

### 50. skill-lens-data-privacy
- **Purpose:** Zero-hardcoding credential invariants, OS-backed secure credential storage (Windows DPAPI / Keychain), student PII protection, GitHub privacy boundaries, and prompt-injection defenses.
- **When to activate:** Handling API keys, storing credentials, managing sensitive student data, sanitizing prompts, and auditing data safety.
- **Relevant project phase:** Phase 2 (Domain Modeling) & Phase 13 (Local Persistence & OS-Backed Storage).
- **Scripts / resources to use:** Windows DPAPI wrappers, PII scrubbing utilities, prompt sanitizers, local scripts/secret-scan.py.
