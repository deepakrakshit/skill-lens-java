#!/usr/bin/env python3
"""
Feature parity verification tool for SkillLens.
Compares the Java desktop codebase against the reference feature inventory
across all 10 essential functional capability domains.
"""

import json
from pathlib import Path
import re
import sys

REFERENCE_CAPABILITY_MATRIX = [
    {
        "id": "CAP-01",
        "domain": "Resume Ingestion & Parsing",
        "keywords": ["pdfbox", "parser", "resume", "extracttext", "pdfparser", "documentparser"],
        "description": "Extract text and metadata from candidate resumes (PDF/DOCX).",
    },
    {
        "id": "CAP-02",
        "domain": "Candidate Domain Model & Persistence",
        "keywords": ["candidaterepository", "candidateentity", "candidate", "jobrole", "evaluationentity"],
        "description": "JPA entities, repositories, and persistence for candidates and roles.",
    },
    {
        "id": "CAP-03",
        "domain": "Skill Extraction & Ontology",
        "keywords": ["skillextractor", "skillservice", "skillsynonym", "taxonomy", "skillcategory"],
        "description": "Deterministic and LLM-assisted skill extraction and categorization.",
    },
    {
        "id": "CAP-04",
        "domain": "GitHub Profile Client",
        "keywords": ["githubclient", "githubservice", "githubadapter", "repoanalyzer"],
        "description": "Fetch repositories, commit metrics, and primary languages from GitHub.",
    },
    {
        "id": "CAP-05",
        "domain": "LeetCode Stats Client",
        "keywords": ["leetcodeclient", "leetcodeservice", "graphqlclient", "problemstats"],
        "description": "Query LeetCode problem-solving stats via GraphQL/REST API.",
    },
    {
        "id": "CAP-06",
        "domain": "Codeforces Contest Client",
        "keywords": ["codeforcesclient", "codeforcesservice", "contestrating"],
        "description": "Fetch user contest history and rank from Codeforces official API.",
    },
    {
        "id": "CAP-07",
        "domain": "Groq LLM Evaluation Engine",
        "keywords": ["groqclient", "chatclient", "springai", "evaluationservice", "groqadapter"],
        "description": "Structured evaluation, scoring rubrics, and feedback generation via LLM.",
    },
    {
        "id": "CAP-08",
        "domain": "Skill Gap & Job Matching",
        "keywords": ["skillgapanalyzer", "jobmatchservice", "gapscore", "matchpercentage"],
        "description": "Analyze match percentage and pinpoint skill deficiencies for job roles.",
    },
    {
        "id": "CAP-09",
        "domain": "Interview Question Generator & Local RAG",
        "keywords": ["questiongenerator", "interviewservice", "retrieval", "ragadvisor", "ragservice"],
        "description": "Generate targeted technical interview questions using candidate gap context.",
    },
    {
        "id": "CAP-10",
        "domain": "JavaFX Desktop UI Dashboard",
        "keywords": ["mainview", "dashboardcontroller", "candidatetable", "javafx", "fxmlview"],
        "description": "Interactive desktop dashboard, charts, candidate detail views, and theming.",
    },
]


def evaluate_parity(root: Path) -> dict[str, object]:
    """Inspect project source files to check capability parity."""
    src_dir = root / "src"
    findings = {}

    all_source_text = ""
    if src_dir.exists():
        for f in src_dir.rglob("*.java"):
            try:
                all_source_text += f.read_text(encoding="utf-8", errors="replace").lower() + "\n"
            except Exception:
                pass

    total = len(REFERENCE_CAPABILITY_MATRIX)
    implemented_count = 0
    results = []

    for cap in REFERENCE_CAPABILITY_MATRIX:
        matched_keywords = []
        if all_source_text:
            for kw in cap["keywords"]:
                if kw in all_source_text:
                    matched_keywords.append(kw)

        is_present = len(matched_keywords) > 0
        if is_present:
            implemented_count += 1

        results.append({
            "id": cap["id"],
            "domain": cap["domain"],
            "description": cap["description"],
            "present": is_present,
            "matched_keywords": matched_keywords,
        })

    coverage_pct = round((implemented_count / total) * 100, 1) if total > 0 else 0.0

    return {
        "total_capabilities": total,
        "implemented_capabilities": implemented_count,
        "coverage_percentage": coverage_pct,
        "capabilities": results,
    }


def main() -> int:
    """Run migration parity check."""
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(encoding="utf-8")
        except Exception:
            pass

    root = Path(__file__).resolve().parents[3]  # skill-lens root
    res = evaluate_parity(root)

    print("=" * 60)
    print("SkillLens Migration Feature Parity Matrix")
    print("=" * 60)
    print(f"Overall Parity Coverage: {res['coverage_percentage']}% ({res['implemented_capabilities']}/{res['total_capabilities']})")
    print("-" * 60)

    for c in res["capabilities"]:
        status = "[DONE] IMPLEMENTED" if c["present"] else "[TODO] PLANNED / PENDING REBUILD"
        print(f"[{c['id']}] {c['domain']:<40} : {status}")
        print(f"       {c['description']}")

    print("=" * 60)
    print("Note: Pre-rebuild benchmark baseline active. 0% is expected prior to rebuild start.")
    print("=" * 60)
    return 0


if __name__ == "__main__":
    sys.exit(main())
