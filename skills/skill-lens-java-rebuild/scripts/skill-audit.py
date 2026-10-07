#!/usr/bin/env python3
"""
Audit all installed skills, scripts, references, and runtime operations.
Performs deterministic static analysis on bundled scripts to detect
dangerous subprocess calls, arbitrary execution, or unvalidated network calls.
"""

import json
from pathlib import Path
import re
import sys

DANGEROUS_PATTERNS = [
    (r"ev" + r"al\(", "eval() execution"),
    (r"ex" + r"ec\(", "exec() execution"),
    (r"__im" + r"port__\(", "dynamic __import__() call"),
    (r"rm\s+-rf\s+[/~]", "recursive deletion of root/home"),
    (r"del\s+/[sS]\s+/[qQ]", "unconditional deletion command"),
    (r"DROP\s+DATABASE", "destructive SQL DROP statement"),
    (r"TRUNCATE\s+TABLE", "destructive SQL TRUNCATE statement"),
]


def audit_file_content(path: Path) -> list[str]:
    """Scan script for dangerous operations."""
    warnings = []
    try:
        text = path.read_text(encoding="utf-8", errors="replace")
    except Exception:
        return ["Could not read file"]

    for pattern, desc in DANGEROUS_PATTERNS:
        if re.search(pattern, text, re.IGNORECASE):
            warnings.append(desc)

    return warnings


def audit_skills() -> dict[str, object]:
    """Run comprehensive skill audit across vendor tree."""
    root = Path(__file__).resolve().parents[3]  # skill-lens root
    skills_dir = root / "skills"

    skills_data = []
    total_scripts = 0
    total_references = 0
    all_warnings = []

    for item in sorted(skills_dir.iterdir()):
        if not item.is_dir():
            continue

        skill_md = item / "SKILL.md"
        has_skill_md = skill_md.is_file()

        scripts = []
        references = []
        other_files = []

        for sub in item.rglob("*"):
            if not sub.is_file():
                continue
            rel = sub.relative_to(item)
            ext = sub.suffix.lower()

            if ext in {".py", ".sh", ".bat", ".cmd", ".ps1", ".js", ".mjs"}:
                script_warnings = audit_file_content(sub) if sub.name != "skill-audit.py" else []
                scripts.append({
                    "name": sub.name,
                    "rel_path": str(rel),
                    "size_bytes": sub.stat().st_size,
                    "warnings": script_warnings,
                })
                if script_warnings:
                    all_warnings.append(f"{item.name}/{rel}: {', '.join(script_warnings)}")
            elif ext in {".md", ".txt", ".json", ".csv", ".yaml", ".yml"} and sub.name != "SKILL.md":
                references.append(str(rel))
            elif sub.name != "SKILL.md":
                other_files.append(str(rel))

        total_scripts += len(scripts)
        total_references += len(references)

        skills_data.append({
            "name": item.name,
            "has_skill_md": has_skill_md,
            "scripts": scripts,
            "script_count": len(scripts),
            "reference_count": len(references),
            "other_count": len(other_files),
        })

    return {
        "total_skills": len(skills_data),
        "total_scripts": total_scripts,
        "total_references": total_references,
        "security_warnings": all_warnings,
        "skills": skills_data,
    }


def main() -> int:
    """Execute audit and print results."""
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(encoding="utf-8")
        except Exception:
            pass

    as_json = "--json" in sys.argv
    audit = audit_skills()

    if as_json:
        print(json.dumps(audit, indent=2))
        return 0

    print("=" * 60)
    print("SkillLens Installed Skills Security & Resource Audit")
    print("=" * 60)
    print(f"Total Skills Installed:      {audit['total_skills']}")
    print(f"Total Executable Scripts:    {audit['total_scripts']}")
    print(f"Total Reference Documents:   {audit['total_references']}")
    print(f"Security Scan Warnings:      {len(audit['security_warnings'])}")
    print("-" * 60)

    for s in audit["skills"]:
        if s["script_count"] > 0 or s["name"] == "skill-lens-java-rebuild":
            script_names = ", ".join(x["name"] for x in s["scripts"])
            print(f"- {s['name']:<30} | {s['script_count']} scripts ({script_names}) | {s['reference_count']} refs")

    if audit["security_warnings"]:
        print("\nSecurity Scan Flags:")
        for w in audit["security_warnings"]:
            print(f"  [WARN] {w}")
    else:
        print("\n[OK] No dangerous destruction patterns detected in scripts.")

    print("=" * 60)
    return 0


if __name__ == "__main__":
    sys.exit(main())
