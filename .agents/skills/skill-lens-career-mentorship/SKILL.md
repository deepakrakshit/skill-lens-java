---
name: skill-lens-career-mentorship
description: >-
  Guides personalized month-by-month career roadmap generation, competency mapping,
  RAG-grounded AI mentorship conversations, context assembly, and progress tracking.
  Use when generating learning roadmaps, assembling mentorship prompts, or conducting conversational career coaching.
---

# SkillLens Career Mentorship & Roadmap Architecture

This skill defines the technical models, sequencing logic, and conversational coaching workflows that bridge verified candidate gaps to concrete career progression milestones.

---

## 1. Actionable Roadmap Principles

- **No Generic Advice:** Never output vague advice like *"Practice DSA"* or *"Learn coding"*.
- **Causal Connection:** Every action item must explicitly tie together four components:
  $$\text{CURRENT STATE} \longrightarrow \text{IDENTIFIED GAP} \longrightarrow \text{PRACTICAL ACTION} \longrightarrow \text{MEASURABLE OUTCOME}$$
- **GitHub Milestones:** Every learning unit must culminate in a concrete, inspectable GitHub artifact (e.g. repository, pull request, test suite, deployment).

---

## 2. Month-by-Month Roadmap Structure

The baseline curriculum spans four progressive months (with optional expansion for severe gaps):

### Month 1: Core Fundamentals & Immediate Gaps
- **Target Skills:** Highest-priority missing foundational skills required by the target role.
- **Theory & Concepts:** Architecture specifications, memory models, protocol basics.
- **Practical Exercises:** Implementing foundational modules in an existing or new project.
- **GitHub Milestone:** Scaffold new repository with automated CI workflows, initial commit structure, and test harness.

### Month 2: Deep Architectural Implementation
- **Target Skills:** Secondary required skills and primary preferred skills.
- **Hands-On Project:** Developing a full-featured service or component addressing the gap.
- **Quality Requirements:** Unit test coverage (> 80%), integration tests, input validation.
- **GitHub Milestone:** Feature pull requests with clean commit histories and descriptive commit messages.

### Month 3: Production Readiness & Tooling
- **Target Skills:** Deployment, containerization, observability, and performance optimization.
- **Practical Tasks:** Dockerfile creation, container orchestration, caching layers, logging pipelines.
- **GitHub Milestone:** Production-grade deployment workflow (GitHub Actions, container registry integration).

### Month 4: Portfolio Polish & Technical Interview Readiness
- **Target Skills:** System design communication, comprehensive documentation, edge-case hardening.
- **Deliverables:** Architectural README with system diagrams, benchmark reports, live demo link.
- **GitHub Milestone:** Tagged release (`v1.0.0`) with release notes and comprehensive user documentation.

---

## 3. RAG-Powered AI Mentorship Engine

The AI mentor acts as a personalized technical advisor grounded in the student's verified profile:

### MentorContextAssembler Protocol:
To keep prompt context focused and token-efficient, the assembler extracts:
1. Target Role & Role Fit Summary (Overall score, top 3 strengths, top 3 gaps).
2. Verified Skills vs. Unverified Claims list.
3. Current Month Roadmap Objectives.
4. Curated technical guidance chunks matching the user's active question.
5. Recent conversation history (last 5 message pairs in the current session).

### Grounding & Persona Guidelines:
- **Tone:** Encouraging, pragmatic, senior technical lead persona.
- **Factual Invariant:** The mentor never fabricates background details or claims the student worked at companies not listed in their verified profile.
- **Action-Oriented:** Every answer concludes with a concrete coding or repository action.
