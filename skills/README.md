# SkillLens Local Agent Skills Toolkit

This directory (`skills/`) serves as the project-owned vendor and source repository for all specialized engineering skills utilized by the Antigravity agent during the end-to-end migration of SkillLens from Python + React into a native modular Java 25 desktop application.

---

## 1. Directory Structure & Architecture

```
skill-lens/
├── .agents/
│   └── skills/                         # Active Antigravity workspace skill discovery location
│       ├── [specialist skills]
│       └── skill-lens-java-rebuild/
└── skills/                             # Project-owned vendor and source repository
    ├── README.md                       # Toolkit overview and architecture documentation
    ├── manifest.json                   # Machine-readable inventory of all 40 installed skills
    ├── ACTIVE_SKILLS.md                # Phase-to-skill activation map
    ├── SKILL_SETUP_REPORT.md           # Audit report, script execution records, and verification gates
    ├── skill-lens-java-rebuild/        # Local meta-orchestrator skill
    │   ├── SKILL.md                    # Master execution workflow
    │   └── scripts/                    # Deterministic local helper scripts
    │       ├── check-environment.py
    │       ├── verify-skill-tree.py
    │       ├── skill-audit.py
    │       ├── run-java-build-check.py
    │       ├── package-readiness-check.py
    │       ├── dependency-check.py
    │       ├── secret-scan.py
    │       ├── reference-boundary-check.py
    │       └── migration-parity-check.py
    ├── javafx-*                        # 16 JavaFX desktop engineering skills
    ├── spring-* / java-*               # Spring Boot 4.1.x, Spring Data JPA, Spring AI skills
    ├── dr-jskill/                      # Java 25 & Spring Boot executable generator skill
    ├── sota-jvm/ & java-architect/     # Modern JVM (Java 25) & enterprise patterns
    ├── software-* & migration-*        # Architecture archaeology and migration engineering
    ├── api-*                           # Strongly-typed API client and adapter engineering
    ├── junit-* & qa-*                  # Automated testing and verification methodology
    ├── secure-software-engineering/    # Desktop security and credential hardening
    ├── maven-tools/                    # Live dependency verification
    └── ui-ux-pro-max/                  # UI design intelligence and JavaFX styling guidelines
```

---

## 2. Engineering Domain Coverage (A through AF)

| Domain | Description | Primary Owning Skills |
| :--- | :--- | :--- |
| **A** | Existing-project archaeology / reverse engineering | `software-architecture-analysis` |
| **B** | Python → Java migration | `migration-engineering` |
| **C** | React → JavaFX desktop UI migration | `javafx-real-world-application-patterns`, `ui-ux-pro-max` |
| **D** | Java 25 engineering | `sota-jvm`, `dr-jskill` |
| **E** | JavaFX application architecture | `javafx-architecture-frameworks`, `javafx-reactive-binding-state` |
| **F** | JavaFX UI / layout / navigation | `javafx-ui-layout-navigation` |
| **G** | JavaFX FXML / CSS | `javafx-fxml-controls-css`, `javafx-theming-icons-styling` |
| **H** | JavaFX desktop application lifecycle | `javafx-application-lifecycle` |
| **I** | JavaFX concurrency / non-blocking UI | `javafx-concurrency-services` |
| **J** | JavaFX forms / validation / preferences | `javafx-forms-validation-preferences`, `javafx-ecosystem-controls-productivity` |
| **K** | JavaFX file workflows | `javafx-file-workflows-search` |
| **L** | JavaFX dashboards / charts | `javafx-data-visualization-dashboards` |
| **M** | JavaFX theming / icons / styling | `javafx-theming-icons-styling`, `ui-ux-pro-max` |
| **N** | JavaFX desktop shell integration | `javafx-desktop-shell-integration` |
| **O** | JavaFX testing / packaging / distribution | `javafx-testing-packaging-distribution` |
| **P** | Spring Boot | `java-springboot`, `create-spring-boot-java-project`, `dr-jskill` |
| **Q** | Spring Data JPA / Hibernate | `spring-data-jpa`, `jpa-query-reviewer` |
| **R** | Spring AI / LLM / RAG | `spring-ai` |
| **S** | HTTP / API client engineering | `api-sdk-generator` |
| **T** | External API integrations | `api-sdk-generator`, `api-inference-from-files` |
| **U** | Database architecture | `spring-data-jpa`, `jpa-query-reviewer` |
| **V** | Migration engineering | `migration-engineering` |
| **W** | Software architecture | `software-architecture`, `java-architect` |
| **X** | Backend engineering | `backend-engineering` |
| **Y** | Secure software engineering | `secure-software-engineering` |
| **Z** | QA / automated testing | `qa-methodology`, `junit-5-skill`, `java-junit-test-generator` |
| **AA** | Verification / acceptance testing | `verification-methodology` |
| **AB** | Debugging / root-cause analysis | `systematic-debugging` |
| **AC** | Technical documentation | `technical-documentation` |
| **AD** | Gradle / build / dependency management | `maven-tools`, `sota-jvm` |
| **AE** | Windows packaging / jlink / jpackage | `javafx-testing-packaging-distribution`, `package-readiness-check.py` |
| **AF** | Dependency / version verification | `maven-tools`, `dr-jskill`, `dependency-check.py` |

---

## 3. Local Helper Scripts Runbook

Run helper scripts directly from project root:

```bash
# Audit toolchains and environment readiness (JDK 25, jpackage, Gradle, Node, Git)
python skills/skill-lens-java-rebuild/scripts/check-environment.py

# Verify skill tree and manifest consistency across vendor and agent trees
python skills/skill-lens-java-rebuild/scripts/verify-skill-tree.py

# Perform security and resource audit over all installed skills and scripts
python skills/skill-lens-java-rebuild/scripts/skill-audit.py

# Validate Java build readiness or execute build test suite
python skills/skill-lens-java-rebuild/scripts/run-java-build-check.py

# Verify jpackage, jlink, and WiX Toolset for Windows desktop packaging
python skills/skill-lens-java-rebuild/scripts/package-readiness-check.py

# Validate stack dependency baseline (Java 25, JavaFX 25, Spring Boot 4.1.x)
python skills/skill-lens-java-rebuild/scripts/dependency-check.py

# Scan repository for hardcoded API keys, tokens, or credentials
python skills/skill-lens-java-rebuild/scripts/secret-scan.py

# Enforce clean architecture boundary against reference directories
python skills/skill-lens-java-rebuild/scripts/reference-boundary-check.py

# Track 10-point functional capability parity against reference inventory
python skills/skill-lens-java-rebuild/scripts/migration-parity-check.py
```

---

## 4. Execution Policy & Safety Gates

1. **Never Hardcode Anything:** All credentials, endpoints, models, and file paths must be externally parameterized via environment variables, config schemas, or user preferences.
2. **Strict Boundary Isolation:** Production code must never import, load, or depend on files inside reference directories.
3. **Execution Before Assumptions:** When third-party skills provide executable scripts (such as `dr-jskill` or `ui-ux-pro-max`), run their diagnostics and capture evidence before proceeding.
