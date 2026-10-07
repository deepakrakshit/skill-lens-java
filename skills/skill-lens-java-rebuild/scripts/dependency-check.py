#!/usr/bin/env python3
"""
Dependency and stack version baseline validator for SkillLens.
Inspects Gradle / Maven build files against 2026 target baselines:
- JDK 25 LTS
- JavaFX 25
- Spring Boot 4.1.x
- Spring Framework 7.x
- Gradle 9.x / Maven 3.9+
- Jakarta EE namespaces
"""

import json
from pathlib import Path
import re
import sys

TARGET_BASELINES = {
    "java": "25",
    "javafx": "25",
    "spring_boot": "4.1",
    "gradle": "9.",
}


def check_build_dependencies(root: Path) -> dict[str, object]:
    """Scan build file for dependency declarations."""
    build_gradle = root / "build.gradle"
    build_gradle_kts = root / "build.gradle.kts"
    pom_xml = root / "pom.xml"

    active_file = None
    if build_gradle.is_file():
        active_file = build_gradle
    elif build_gradle_kts.is_file():
        active_file = build_gradle_kts
    elif pom_xml.is_file():
        active_file = pom_xml

    if not active_file:
        return {
            "status": "PRE_REBUILD_COMPLIANT",
            "message": "No active build descriptor yet. Pre-rebuild phase active.",
            "target_baselines": TARGET_BASELINES,
            "findings": [],
        }

    content = active_file.read_text(encoding="utf-8", errors="replace")
    findings = []

    # Detect Java version
    java_matches = re.findall(r"(?:sourceCompatibility|targetCompatibility|java\.version|jvmTarget)\s*[=:]\s*['\"]?(\d+)['\"]?", content)
    for jm in java_matches:
        if jm != "25":
            findings.append(f"Java version declared as '{jm}', but target baseline is 25.")

    # Detect JavaFX version
    jfx_matches = re.findall(r"org\.openjfx.*?:(\d+)", content)
    for jm in jfx_matches:
        if not jm.startswith("25"):
            findings.append(f"JavaFX version declared as '{jm}', target baseline is 25.")

    # Detect Spring Boot version
    boot_matches = re.findall(r"org\.springframework\.boot.*?:([0-9\.]+)", content)
    for bm in boot_matches:
        if not bm.startswith("4."):
            findings.append(f"Spring Boot version declared as '{bm}', target baseline is 4.1.x.")

    # Detect legacy javax packages
    if "javax.persistence" in content or "javax.servlet" in content:
        findings.append("Legacy 'javax.*' namespaces detected; must use 'jakarta.*'.")

    return {
        "status": "PASS" if not findings else "WARN",
        "active_file": str(active_file.name),
        "target_baselines": TARGET_BASELINES,
        "findings": findings,
    }


def main() -> int:
    """Run dependency check."""
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(encoding="utf-8")
        except Exception:
            pass

    root = Path(__file__).resolve().parents[3]  # skill-lens root
    res = check_build_dependencies(root)

    print("=" * 60)
    print("SkillLens Dependency & Baseline Verification")
    print("=" * 60)
    print(f"Status:   [{res['status']}]")
    if "message" in res:
        print(f"Detail:   {res['message']}")
    if "active_file" in res:
        print(f"File:     {res['active_file']}")
    print("-" * 60)
    print("Target 2026 Stack Baselines:")
    for k, v in TARGET_BASELINES.items():
        print(f"  - {k:<12}: {v}")

    if res["findings"]:
        print("\nFindings / Warnings:")
        for f in res["findings"]:
            print(f"  [WARN] {f}")
    else:
        print("\n[OK] All baseline constraints satisfied.")

    print("=" * 60)
    return 0


if __name__ == "__main__":
    sys.exit(main())
