---
name: skill-lens-resume-engineering
description: >-
  Guides structured candidate profile modeling, skill alias normalization, evidence-grounded resume rewriting,
  ATS-friendly layout design across 10 professional templates, and Java-native PDF/DOCX generation.
  Use when normalizing extracted resumes, improving resume bullets, selecting templates, or exporting documents.
---

# SkillLens Resume Engineering

This skill defines the data models, normalization algorithms, ethical rewriting constraints, and document generation standards for transforming student resumes into verified, ATS-friendly artifacts.

---

## 1. Skill Extraction & Alias Normalization

Extracted resume text undergoes deterministic tokenization, n-gram matching (1-to-4 grams), and ontology lookup to isolate skill references:
- **Taxonomy Categories:**
  - Programming Languages (Java, Python, C++, TypeScript, Go)
  - Frameworks & Libraries (Spring Boot, React, Angular, Vue, Express, PyTorch)
  - Databases (PostgreSQL, MySQL, MongoDB, Redis, SQLite)
  - Cloud & DevOps (AWS, Docker, Kubernetes, GitHub Actions, Terraform)
  - Architecture & Concepts (REST, GraphQL, Microservices, CI/CD, OOP)
- **Alias Resolution:**
  - Map variations to canonical forms:
    - `ReactJS`, `React.js`, `React` → `React`
    - `NodeJS`, `Node.js`, `Node` → `Node.js`
    - `Postgres`, `PostgreSQL DB` → `PostgreSQL`
    - `K8s` → `Kubernetes`
    - `SpringBoot` → `Spring Boot`
  - **Preserve Source Evidence:** Maintain both the original verbatim text and the canonical ID in the candidate skill model.

---

## 2. Ethical Rewriting Boundary (Non-Fabrication Invariant)

When rewriting bullet points or summaries:
- **STRICT PROHIBITION:**
  - NEVER invent companies, employers, job titles, or dates.
  - NEVER invent unverified degrees, schools, or certifications.
  - NEVER add technologies, languages, or tools not present in the student's resume or verified GitHub evidence.
  - NEVER manufacture fabricated metrics (e.g. inventing *"increased throughput by 42%"* when the candidate provided no quantitative claim).
- **PERMITTED REFINEMENTS:**
  - Structure bullet points using strong action verbs (Architected, Engineered, Implemented, Streamlined).
  - Clarify technical context and role responsibilities based on verified GitHub projects.
  - Apply the STAR / XYZ formula (*"Accomplished [X] as measured by [Y] by doing [Z]"*) to existing achievements.
  - Fix grammatical errors, typos, and awkward phrasing.

---

## 3. Ten ATS-Friendly Resume Templates

The system provides 10 distinct, professional, ATS-friendly templates. Terminology rule: Always describe templates as **"ATS-friendly"**; never claim they are "ATS approved" by an external certifying body.

1. **Classic Professional:** Traditional single-column serif/sans-serif design with horizontal dividing rules; standard corporate baseline.
2. **Modern Minimal:** Clean whitespace-centric layout with subtle color accents and modern sans-serif typography.
3. **Software Engineer:** Technology-first layout highlighting technical skills, GitHub repositories, and architectural contributions near the top.
4. **Technical Compact:** Dense layout maximizing content density for multi-project software developers without exceeding one page.
5. **Executive Clean:** Balanced layout emphasizing leadership, architectural decisions, and project lifecycle stewardship.
6. **Product / Engineering:** Dual-emphasis layout highlighting user impact, feature delivery, and underlying technical stacks.
7. **Data / Analytics:** Focused on data pipelines, modeling frameworks, databases, and analytical tools.
8. **Academic Technical:** Emphasizes research projects, degrees, coursework, publications, and competitive programming ratings.
9. **Startup Modern:** Highlighting rapid delivery, end-to-end full-stack projects, and hackathon prototypes.
10. **One-Page Impact:** Precision-engineered single-page format for new graduates and early-career engineers.

---

## 4. ATS-Friendly Architectural Rules

- **Single Flow Layout:** Avoid complex multi-column frames or floating text boxes that break screen readers and legacy ATS parsers.
- **Standard Headings:** Use conventional section headings (*"Experience"*, *"Education"*, *"Projects"*, *"Technical Skills"*).
- **Standard Typography:** Standard system fonts (Helvetica, Arial, Times New Roman, Calibri, Roboto).
- **Accessible Links:** Embed clickable hyperlinks with readable fallback text.

---

## 5. Java-Native Document Generation (PDF & DOCX)

Generated resumes must be real, native documents—never HTML screenshots or browser printouts:
- **DOCX Generation:** Built using Apache POI (`XWPFDocument`). Apply table styles, paragraph spacing, and bold inline runs natively.
- **PDF Generation:** Built using Apache PDFBox or OpenPDF with programmatic layout calculations:
  - Calculate line wrapping and margins deterministically.
  - Manage page overflow and prevent orphaned headings.
  - Validate output visual integrity (margins: 0.5 to 0.75 in, line spacing: 1.15 to 1.3).
