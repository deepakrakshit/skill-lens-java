---
name: skill-lens-evidence-analysis
description: >-
  Provides the multi-signal skill verification model, deterministic confidence scoring,
  buzzword and exaggeration detection, and evidence graph construction.
  Use when comparing resume claims against GitHub activity, assigning verification statuses, or calculating role fit.
---

# SkillLens Evidence Analysis & Verification Model

This skill defines the evidence-based verification engine that grounds resume claims against actual public GitHub engineering activity.

---

## 1. Core Invariant: Commits & Repositories Are Signals, Not Proof

The system operates on multi-signal probabilistic evidence rather than naive quantitative heuristics:
- **Never Assume:**
  - High commit count = high skill (avoids skew from formatting bots or trivial micro-commits).
  - High star count = individual mastery (stars measure project popularity, not contributor competence).
  - Merely forking a repo = skill adoption.
  - Presence of a language in repo stats = full framework proficiency.
- **Evidence Weighting:** Evidence requires corroboration across multiple dimensions: project architecture, dependency declarations, commit message semantics, code modifications, and recency.

---

## 2. Verification Status Hierarchy

Every skill evaluated by the system receives one of five deterministic verification statuses:

1. **`VERIFIED` (Strong Multi-Signal Corroboration):**
   - Present on resume.
   - Corroborated by active original repositories where the skill is a primary or secondary technology.
   - Verified dependency file entries (e.g. `pom.xml`, `package.json`, `requirements.txt`).
   - Multiple substantive commits authored by the candidate showing iterative development.
   - Recent activity within the last 18 months.

2. **`PARTIALLY VERIFIED` (Moderate Evidence / Limited Depth):**
   - Present on resume.
   - Corroborated by at least one repository or peripheral usage.
   - Low commit frequency or older project history (> 18 months inactive).
   - Basic implementation without demonstrated advanced architectural patterns.

3. **`WEAK EVIDENCE` (Incidental / Minimal Signal):**
   - Present on resume.
   - Only appears in incidental configuration, minor bugfix commits, or tutorial clones.
   - Insufficient depth to verify independent production competence.

4. **`CLAIMED / UNVERIFIED` (Resume Claim Without Public Evidence):**
   - Explicitly listed or claimed on the resume.
   - No supporting repositories, dependency declarations, or commit history found in the student's public GitHub footprint.
   - *Interpretation:* The student may possess this skill from private repositories, coursework, or internships, but public proof is absent.

5. **`MISSING` (Unaddressed Role Requirement):**
   - Required or preferred for the student's target role.
   - Neither claimed on the resume nor supported by any GitHub evidence.

---

## 3. Buzzword & Exaggeration Detection

When a candidate makes strong qualitative claims (e.g. *"Senior Python Architect"*, *"Expert in Kubernetes"*, *"Production Microservices Specialist"*), the system evaluates claim strength against available evidence:
- **Tone & Language Policy:** Always remain professional, objective, and evidence-grounded. Never use defamatory terms such as *"liar"*, *"fake"*, or *"fraud"*.
- **Standardized Explanations:**
  - *No Supporting Evidence:* *"Claim not supported by available public GitHub evidence."*
  - *Disproportionate Claim:* *"Claim strength exceeds available public evidence. Resume claims senior-level mastery in [Skill], but public projects demonstrate early foundational usage."*
  - *Missing Tooling:* *"Resume highlights container orchestration, but no Dockerfiles or deployment manifests were detected in public repositories."*

---

## 4. Deterministic Role Fit & Scoring Formulas

Numeric scores are computed using pure mathematical formulas—never hallucinated by an LLM:

### A. Role Fit Score (0 to 100):
$$\text{RoleFitScore} = \left( 0.50 \times \text{ReqCoverage} \right) + \left( 0.25 \times \text{PrefCoverage} \right) + \left( 0.25 \times \text{VerifiedEvidenceRatio} \right)$$

Where:
- $\text{ReqCoverage} = \frac{\sum \text{Score}(\text{Required Skills})}{\text{Total Required Skills}} \times 100$
- $\text{PrefCoverage} = \frac{\sum \text{Score}(\text{Preferred Skills})}{\text{Total Preferred Skills}} \times 100$
- Scoring weights per skill status:
  - `VERIFIED`: 1.0
  - `PARTIALLY VERIFIED`: 0.6
  - `WEAK EVIDENCE`: 0.3
  - `CLAIMED / UNVERIFIED`: 0.15
  - `MISSING`: 0.0

### B. Gap Severity Ranking:
Sort identified skill gaps into three actionable tiers:
- **High Priority:** Required skills with status `MISSING` or `CLAIMED / UNVERIFIED`.
- **Medium Priority:** Required skills with status `PARTIALLY VERIFIED` or preferred skills with status `MISSING`.
- **Low Priority:** Preferred skills with status `PARTIALLY VERIFIED` or peripheral tools.
