#!/usr/bin/env python3
"""
Java build check utility for SkillLens.
Detects active Gradle or Maven build configurations and executes verification.
Gracefully validates toolchain when build files have not yet been scaffolded.
"""

from pathlib import Path
import shutil
import subprocess
import sys


def detect_build_system(root: Path) -> dict[str, object]:
    """Detect presence of build configurations in workspace."""
    build_gradle = root / "build.gradle"
    build_gradle_kts = root / "build.gradle.kts"
    pom_xml = root / "pom.xml"
    gradlew_bat = root / "gradlew.bat"
    gradlew_sh = root / "gradlew"

    has_gradle = build_gradle.is_file() or build_gradle_kts.is_file()
    has_maven = pom_xml.is_file()
    has_gradlew = gradlew_bat.is_file() or gradlew_sh.is_file()

    return {
        "has_gradle": has_gradle,
        "has_maven": has_maven,
        "has_gradlew": has_gradlew,
        "build_gradle": str(build_gradle) if build_gradle.is_file() else None,
        "build_gradle_kts": str(build_gradle_kts) if build_gradle_kts.is_file() else None,
        "pom_xml": str(pom_xml) if pom_xml.is_file() else None,
    }


def main() -> int:
    """Execute build verification or readiness check."""
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(encoding="utf-8")
        except Exception:
            pass

    root = Path(__file__).resolve().parents[3]  # skill-lens root
    detection = detect_build_system(root)

    print("=" * 60)
    print("SkillLens Java Build Verification")
    print("=" * 60)

    if not detection["has_gradle"] and not detection["has_maven"]:
        print("[STATUS] NO_BUILD_FILES_PRESENT")
        print("Reason: Project is currently in pre-rebuild setup phase.")
        print("No Java application code or build descriptors have been created yet.")
        print("Build toolchain readiness:")
        java_bin = shutil.which("java")
        javac_bin = shutil.which("javac")
        gradle_bin = shutil.which("gradle")
        print(f"  Java Runtime:   {java_bin or 'NOT_FOUND'}")
        print(f"  Java Compiler:  {javac_bin or 'NOT_FOUND'}")
        print(f"  System Gradle:  {gradle_bin or 'Will be provided via gradlew wrapper upon project scaffolding'}")
        print("[OK] Pre-rebuild build gate condition satisfied.")
        print("=" * 60)
        return 0

    print("[STATUS] ACTIVE_BUILD_DETECTED")
    if detection["has_gradle"]:
        cmd = ["gradlew.bat" if sys.platform == "win32" else "./gradlew", "check"]
        print(f"Running: {' '.join(cmd)}")
        res = subprocess.run(cmd, cwd=root, check=False)
        return res.returncode
    elif detection["has_maven"]:
        cmd = ["mvnw.cmd" if sys.platform == "win32" else "./mvnw", "test"]
        print(f"Running: {' '.join(cmd)}")
        res = subprocess.run(cmd, cwd=root, check=False)
        return res.returncode

    return 0


if __name__ == "__main__":
    sys.exit(main())
