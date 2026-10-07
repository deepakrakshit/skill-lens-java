#!/usr/bin/env python3
"""
Verify skill tree integrity across skills/ and .agents/skills/.
Validates manifest consistency, presence of SKILL.md files,
YAML frontmatter format, and referenced resources.
"""

import json
from pathlib import Path
import re
import sys


def parse_frontmatter(skill_md_path: Path) -> dict[str, str]:
    """Extract and parse YAML frontmatter from SKILL.md without external dependencies."""
    content = skill_md_path.read_text(encoding="utf-8", errors="replace")
    if not content.startswith("---"):
        return {}

    parts = content.split("---", 2)
    if len(parts) < 3:
        return {}

    frontmatter_raw = parts[1]
    data: dict[str, str] = {}
    current_key = None
    multiline_buf = []

    for line in frontmatter_raw.splitlines():
        # Match key: value
        match = re.match(r"^([A-Za-z0-9_-]+):\s*(.*)$", line)
        if match:
            if current_key and multiline_buf:
                data[current_key] = " ".join(multiline_buf).strip()
                multiline_buf = []
            current_key = match.group(1).strip()
            val = match.group(2).strip()
            if val in (">", ">-", "|", "|-"):
                multiline_buf = []
            else:
                data[current_key] = val
        elif current_key and (line.startswith("  ") or line.startswith("\t")):
            multiline_buf.append(line.strip())

    if current_key and multiline_buf:
        data[current_key] = " ".join(multiline_buf).strip()

    return data


def verify_skill_tree() -> tuple[bool, list[dict[str, object]]]:
    """Verify all skills against manifest.json and directory structure."""
    root = Path(__file__).resolve().parents[3]  # skill-lens root
    manifest_path = root / "skills" / "manifest.json"
    skills_dir = root / "skills"
    agents_skills_dir = root / ".agents" / "skills"

    if not manifest_path.exists():
        print(f"ERROR: Manifest file not found at {manifest_path}")
        return False, []

    with open(manifest_path, "r", encoding="utf-8") as f:
        manifest_data = json.load(f)

    skills_list = manifest_data.get("skills", [])
    results = []
    all_ok = True

    for entry in skills_list:
        name = entry.get("name")
        vendor_path = skills_dir / name
        agents_path = agents_skills_dir / name

        has_vendor = vendor_path.is_dir()
        has_agents = agents_path.is_dir()
        vendor_md = vendor_path / "SKILL.md"
        agents_md = agents_path / "SKILL.md"

        has_vendor_md = vendor_md.is_file()
        has_agents_md = agents_md.is_file()

        frontmatter = parse_frontmatter(vendor_md) if has_vendor_md else {}
        has_valid_name = bool(frontmatter.get("name"))
        has_valid_desc = bool(frontmatter.get("description"))

        scripts = [
            p.name for p in vendor_path.rglob("*")
            if p.is_file() and p.suffix.lower() in {".py", ".sh", ".bat", ".cmd", ".ps1", ".js", ".mjs"}
        ] if has_vendor else []

        declared_scripts = bool(entry.get("hasScripts", False))
        actual_has_scripts = len(scripts) > 0

        ok = (
            has_vendor
            and has_agents
            and has_vendor_md
            and has_agents_md
            and has_valid_name
            and has_valid_desc
        )

        if not ok:
            all_ok = False

        results.append({
            "name": name,
            "has_vendor_dir": has_vendor,
            "has_agents_dir": has_agents,
            "has_vendor_md": has_vendor_md,
            "has_agents_md": has_agents_md,
            "frontmatter_name": frontmatter.get("name", "MISSING"),
            "has_description": has_valid_desc,
            "scripts_count": len(scripts),
            "manifest_has_scripts": declared_scripts,
            "scripts_match": (declared_scripts == actual_has_scripts),
            "status": "PASS" if ok else "FAIL",
        })

    return all_ok, results


def main() -> int:
    """Run tree verification."""
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(encoding="utf-8")
        except Exception:
            pass

    all_ok, results = verify_skill_tree()
    print("=" * 60)
    print("Skill Tree Integrity Verification")
    print("=" * 60)
    passed = sum(1 for r in results if r["status"] == "PASS")
    failed = len(results) - passed

    for r in results:
        status_icon = "[OK]" if r["status"] == "PASS" else "[FAIL]"
        print(f"{status_icon} {r['name']:<38} (scripts: {r['scripts_count']})")
        if r["status"] != "PASS":
            print(f"    Vendor: {r['has_vendor_dir']} | Agents: {r['has_agents_dir']} | MD: {r['has_vendor_md']}")

    print("-" * 60)
    print(f"Total skills checked: {len(results)} | Passed: {passed} | Failed: {failed}")
    print("=" * 60)
    return 0 if all_ok else 1


if __name__ == "__main__":
    sys.exit(main())
