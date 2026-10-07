#!/usr/bin/env python3
"""
Secret scanning utility for SkillLens repository.
Identifies potential hard-coded API keys, tokens, credentials, and passwords.
Ignores binary files, git history, scratch directories, and third-party skills vendor data.
"""

import json
from pathlib import Path
import re
import sys

# High-confidence credential patterns
SECRET_PATTERNS = [
    (r"gsk_[a-zA-Z0-9]{20,}", "Groq API Key (gsk_...)"),
    (r"ghp_[a-zA-Z0-9]{36}", "GitHub Personal Access Token (ghp_...)"),
    (r"github_pat_[a-zA-Z0-9_]{50,}", "GitHub Fine-Grained PAT"),
    (r"sk-[a-zA-Z0-9]{32,}", "OpenAI / Generic API Key (sk-...)"),
    (r"AIza[0-9A-Za-z-_]{35}", "Google Cloud / Gemini API Key"),
    (r"(?:api[_-]?key|secret|password|auth_token)\s*[:=]\s*['\"][a-zA-Z0-9_\-\.]{16,}['\"]", "High-entropy key assignment"),
]

IGNORED_DIRS = {
    ".git",
    "scratch",
    "node_modules",
    "target",
    "build",
    ".gradle",
    "bin",
    "out",
    ".idea",
    ".vscode",
    "__pycache__",
}


IGNORED_FILES = {
    ".env",
    "sessions.json",
    "conversations.json",
    "coverage.xml",
}


def is_ignored(path: Path, root: Path) -> bool:
    """Determine if path should be skipped."""
    try:
        rel = path.relative_to(root)
    except ValueError:
        return True

    if path.name in IGNORED_FILES or (path.name.startswith(".env.") and path.name != ".env.example"):
        return True

    for part in rel.parts:
        if part in IGNORED_DIRS:
            return True
    return False


def scan_file(file_path: Path, root: Path) -> list[dict[str, object]]:
    """Scan single file for secrets."""
    matches = []
    try:
        content = file_path.read_text(encoding="utf-8", errors="replace")
    except Exception:
        return []

    lines = content.splitlines()
    for idx, line in enumerate(lines, start=1):
        # Skip commented placeholders or sample values
        stripped = line.strip()
        if stripped.startswith("#") and "example" in stripped.lower():
            continue
        if "placeholder" in stripped.lower() or "your_api_key" in stripped.lower():
            continue

        for pattern, label in SECRET_PATTERNS:
            match = re.search(pattern, line)
            if match:
                raw_match = match.group(0)
                # Redact matched text
                redacted = raw_match[:4] + "..." + raw_match[-4:] if len(raw_match) > 8 else "***"
                matches.append({
                    "file": str(file_path.relative_to(root)),
                    "line": idx,
                    "label": label,
                    "preview": redacted,
                })
    return matches


def main() -> int:
    """Run secret scan over project."""
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(encoding="utf-8")
        except Exception:
            pass

    root = Path(__file__).resolve().parents[3]  # skill-lens root
    all_findings = []

    for path in root.rglob("*"):
        if path.is_file() and not is_ignored(path, root):
            # Also skip checking third-party vendor skill test files that mock patterns
            if "skills" in path.parts and ("test" in path.name.lower() or "pattern" in path.name.lower()):
                continue
            findings = scan_file(path, root)
            all_findings.extend(findings)

    print("=" * 60)
    print("SkillLens Secret Scanner Report")
    print("=" * 60)
    print(f"Total potential secrets discovered: {len(all_findings)}")
    print("-" * 60)

    if all_findings:
        for f in all_findings:
            print(f"  [ALERT] {f['file']}:{f['line']} - {f['label']} ({f['preview']})")
        print("\nACTION REQUIRED: Remove hardcoded credentials. Use environment variables.")
        print("=" * 60)
        return 1

    print("[OK] No hard-coded secrets or credentials detected in repository.")
    print("=" * 60)
    return 0


if __name__ == "__main__":
    sys.exit(main())
