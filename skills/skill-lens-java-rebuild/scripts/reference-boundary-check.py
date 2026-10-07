#!/usr/bin/env python3
"""
Boundary enforcement check for SkillLens codebase.
Ensures that new Java production source code, resources, and tests
never reference, import, or load files from the future `secret/` reference clone.
Validates architecture isolation and deployment self-containment.
"""

from pathlib import Path
import re
import sys

ILLEGAL_REFERENCE_PATTERNS = [
    r"['\"](?:\.\./)*secret/",
    r"import\s+.*secret\.",
    r"Paths\.get\(.*['\"]secret['\"]",
    r"new\s+File\(.*['\"]secret['\"]",
    r"classpath:.*secret",
]


def check_boundaries(root: Path) -> list[dict[str, object]]:
    """Scan all project source files outside secret/ for references to secret/."""
    violations = []
    src_dirs = [root / "src", root / "app", root / "backend", root / "frontend"]

    for d in src_dirs:
        if not d.exists():
            continue
        for f in d.rglob("*"):
            if not f.is_file():
                continue
            try:
                content = f.read_text(encoding="utf-8", errors="replace")
            except Exception:
                continue

            for idx, line in enumerate(content.splitlines(), start=1):
                for pat in ILLEGAL_REFERENCE_PATTERNS:
                    if re.search(pat, line):
                        violations.append({
                            "file": str(f.relative_to(root)),
                            "line": idx,
                            "content": line.strip(),
                        })

    return violations


def main() -> int:
    """Run reference boundary check."""
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(encoding="utf-8")
        except Exception:
            pass

    root = Path(__file__).resolve().parents[3]  # skill-lens root
    violations = check_boundaries(root)

    print("=" * 60)
    print("SkillLens Architectural Boundary & Isolation Check")
    print("=" * 60)
    print("Rule: Production code must never depend on or load from `secret/` reference.")
    print("-" * 60)

    if violations:
        print(f"[FAIL] Boundary violations detected ({len(violations)}):")
        for v in violations:
            print(f"  [VIOLATION] {v['file']}:{v['line']} -> {v['content']}")
        print("=" * 60)
        return 1

    print("[OK] Boundary verified: 0 dependencies on external reference directory.")
    print("=" * 60)
    return 0


if __name__ == "__main__":
    sys.exit(main())
