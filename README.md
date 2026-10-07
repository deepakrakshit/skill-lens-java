# SkillLens

> **Evidence-Based Student Skill Verification, Career Intelligence, Mentorship, and Resume Optimization Platform**

---

## Overview

**SkillLens** is an evidence-grounded career intelligence desktop application engineered in **Java 25**, **JavaFX 25**, and **Spring Boot 3.4.3**.

Rather than relying purely on what a candidate claims on their resume, SkillLens bridges the trust gap between job seekers and hiring teams by analyzing three key pillars:

```
    RESUME CLAIMS
          +
 REAL PUBLIC GITHUB EVIDENCE
          +
TARGET ROLE REQUIREMENTS
          ↓
  SKILL VERIFICATION (5-Tier Status)
          ↓
  SKILL GAP & EXAGGERATION ANALYSIS
          ↓
  DETERMINISTIC ROLE FIT (0–100)
          ↓
  CAREER ROADMAP (Month-by-Month)
          ↓
  AI MENTORSHIP (Grounded RAG)
          ↓
  RESUME OPTIMIZATION (Google XYZ & ATS)
```

---

## Core Capabilities

### 1. Document Ingestion and Candidate Extraction
- Ingests resumes in PDF, DOCX, TXT, or pasted raw text using Apache Tika, PDFBox, and Apache POI.
- Extracts structured profile fields, education, work experience, project history, and canonical skill claims.
- Normalizes skill aliases (e.g., "K8s" → "Kubernetes", "ReactJS" → "React").

### 2. Public GitHub Repository Intelligence
- Resolves GitHub handles from usernames or URLs.
- Inspects public repositories, languages, README markdown, and repository topics.
- Analyzes commit activity and inspects the latest 30 commit messages per relevant repository.
- Constructs an evidence graph linking skill claims directly to concrete source code and commit records.

### 3. Multi-Signal Verification and Scoring
- **5-Tier Verification Model**:
  - `VERIFIED`: Concrete code, commits, or project implementations back the claim.
  - `PARTIALLY_VERIFIED`: Related project code or secondary evidence found.
  - `WEAK_EVIDENCE`: Minimal or peripheral commit activity detected.
  - `CLAIMED_UNVERIFIED`: Declared on the resume, but no public evidence found.
  - `MISSING`: Target role requirement absent from both resume and GitHub.
- **Deterministic Role Fit Scoring**: Mathematical calculation (0–100) based on weighted role competencies. The AI is never allowed to hallucinate the final score.
- **Buzzword & Exaggeration Detection**: Flags unsubstantiated declarative claims with objective, respectful phrasing (e.g., *"Claim strength exceeds available public evidence"*).
- **Candidate Fairness**: Candidates without GitHub profiles are categorized as `NOT_PROVIDED` and never penalized with negative evidence.

### 4. Multi-Provider AI Routing and Resilience
- **Google Gemini (Primary & Vision)**: `gemini-3.5-flash` for high-throughput structured extraction, role matching, and vision.
- **Groq (Fallback)**: `openai/gpt-oss-120b` for automatic failover when primary limits or network errors occur.
- **Defensive Demarcation**: All untrusted user or document inputs are encapsulated inside `<<<USER_UNTRUSTED_CONTENT>>>` tags to prevent prompt injection.

### 5. Local Java RAG and Mentorship Engine
- **Zero Python Runtime**: All vector embedding, tokenization, BM25 indexing, and hybrid search occur natively inside the JVM.
- **Dense Vector Search**: 256-dimensional unit-norm dense embeddings combined with BM25 keyword matching via Reciprocal Rank Fusion (RRF).
- **Tenant Isolation**: Vectors and chunks are partitioned by student ID to prevent cross-profile leakage.
- **Evidence-Grounded Coaching**: Conversational AI mentor references candidate repositories and refuses to validate unverified assertions.

### 6. Career Roadmapping and Learning Resources
- Produces structured 4-month progression roadmaps linking verified skill gaps to GitHub project milestones.
- YouTube Data API integration with automatic disk caching and quota conservation.
- Curated offline learning catalog with authoritative documentation and books across 13 industry roles.

### 7. Ethical Resume Optimization and ATS Document Generation
- Rewrites resume bullet points using the Google XYZ formula (*"Accomplished [X] as measured by [Y] by doing [Z]"*).
- **Non-Fabrication Invariant**: The system never invents employers, job titles, degrees, or metrics.
- 10 ATS-friendly professional templates (Standard Classic, Modern Clean, Technical Minimalist, Executive, Compact, Academic, etc.).
- Direct binary document generation to OpenPDF (PDF) and Apache POI (DOCX).

### 8. Recruiter Workspace
- Batch candidate evaluation and ranking against custom or preset role profiles.
- Separate ranking tracks for verified GitHub profiles vs. unverified profiles.

### 9. Modern JavaFX 25 User Interface
- AtlantaFX Cupertino Dark and Light themes.
- 10 comprehensive views: Home, Student Workspace, Evidence Dashboard, Role Fit, Roadmap, Mentorship, Resume Builder, Learning Resources, Recruiter Workspace, and Settings.

---

## Technical Stack

| Component | Technology | Version |
|---|---|---|
| Platform / JDK | Oracle OpenJDK | Java 25 LTS |
| UI Framework | JavaFX / AtlantaFX | 25-ea+19 / 2.0.1 |
| Backend Framework | Spring Boot | 3.4.3 |
| Build Tool | Apache Maven | 3.9.9 |
| Persistence | Spring Data JPA / H2 File-based | 3.4.3 |
| Document Parsing | Apache Tika / PDFBox / POI | 2.9.2 / 3.0.4 / 5.4.0 |
| PDF & DOCX Generation | OpenPDF / Apache POI | 2.0.2 / 5.4.0 |
| AI Clients | Google Gemini REST / Groq REST | HTTP/2 Native |
| Credential Vault | Windows DPAPI (`CryptProtectData`) | JNA 5.14.0 |

---

## Prerequisites

- **Operating System**: Windows 10/11 (x64)
- **Java Development Kit**: JDK 25 LTS (installed with `JAVA_HOME` pointing to JDK 25)
- **Git** (for repository management)

---

## Getting Started

### 1. Build and Run Tests
Run the Maven test suite using the included Maven wrapper:

```powershell
.\mvnw.cmd test
```

All 40 unit and integration tests will execute and verify the pipeline.

### 2. Run the Application
Launch the desktop application via Spring Boot:

```powershell
.\mvnw.cmd spring-boot:run
```

Or run the Java main class directly:

```powershell
java --enable-preview --enable-native-access=ALL-UNNAMED -jar target/skill-lens-1.0.0-SNAPSHOT.jar
```

### 3. Package Executable JAR
Create the standalone executable fat JAR:

```powershell
.\mvnw.cmd clean package -DskipTests
```

The output JAR will be placed at:
```
target/skill-lens-1.0.0-SNAPSHOT.jar
```

### 4. Build Self-Contained Windows Distribution (`app-image`)
Generate a self-contained Windows application bundle containing a dedicated JDK 25 runtime and a native `SkillLens.exe` launcher:

```powershell
New-Item -ItemType Directory -Force -Path 'target/package-input'
Copy-Item 'target/skill-lens-1.0.0-SNAPSHOT.jar' 'target/package-input/SkillLens.jar'
& "$env:JAVA_HOME\bin\jpackage.exe" --type app-image --name SkillLens --input target/package-input --main-jar SkillLens.jar --dest dist --java-options '--enable-preview' --java-options '--enable-native-access=ALL-UNNAMED'
```

The self-contained application will be available at:
```
dist/SkillLens/SkillLens.exe
```

---

## Configuration and Credentials

SkillLens supports zero-hardcoding configuration. In order of priority:
1. **Windows DPAPI Vault**: Configured via the in-app **Settings View** and encrypted using Windows OS user credentials.
2. **Environment Variables**:
   - `GEMINI_API_KEY`: Google Gemini API Key (Primary & Vision AI)
   - `GROQ_API_KEY`: Groq API Key (Fallback AI)
   - `GITHUB_TOKEN`: (Optional) GitHub Personal Access Token to expand rate limits
   - `YOUTUBE_API_KEY`: (Optional) YouTube Data API Key for live playlist queries

---

## Quality and Verification Standards

- **100% Test Pass Rate**: Verified across all 40 automated test suites.
- **Zero Hardcoding**: Dynamically discovered endpoints, keys, and configurations.
- **Zero Python/Node Dependencies**: Clean-room JVM architecture.
- **Zero External Server Requirement**: Local embedded database and in-process AI orchestration.
