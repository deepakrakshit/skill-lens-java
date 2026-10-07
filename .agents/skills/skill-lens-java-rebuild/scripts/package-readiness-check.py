#!/usr/bin/env python3
"""
Windows package readiness check for SkillLens desktop distribution.
Verifies jpackage (JDK 25), jlink, and WiX Toolset for native installer generation.
"""

import json
from pathlib import Path
import shutil
import subprocess
import sys


def check_jpackage() -> dict[str, object]:
    """Verify jpackage from PATH or JDK 25 directory."""
    jp_path = shutil.which("jpackage")
    candidate_jdk25 = Path(r"C:\Program Files\Java\jdk-25.0.4\bin\jpackage.exe")
    if not jp_path and candidate_jdk25.exists():
        jp_path = str(candidate_jdk25)

    if not jp_path:
        return {"status": "FAIL", "path": None, "version": None, "message": "jpackage not found"}

    try:
        proc = subprocess.run(
            [jp_path, "--version"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=False,
            timeout=5,
        )
        ver = proc.stdout.strip() or proc.stderr.strip()
        return {"status": "PASS", "path": jp_path, "version": ver, "message": "jpackage available"}
    except Exception as exc:
        return {"status": "FAIL", "path": jp_path, "version": None, "message": str(exc)}


def check_jlink() -> dict[str, object]:
    """Verify jlink from PATH or JDK 25 directory."""
    jl_path = shutil.which("jlink")
    candidate_jdk25 = Path(r"C:\Program Files\Java\jdk-25.0.4\bin\jlink.exe")
    if not jl_path and candidate_jdk25.exists():
        jl_path = str(candidate_jdk25)

    if not jl_path:
        return {"status": "FAIL", "path": None, "version": None, "message": "jlink not found"}

    try:
        proc = subprocess.run(
            [jl_path, "--version"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=False,
            timeout=5,
        )
        ver = proc.stdout.strip() or proc.stderr.strip()
        return {"status": "PASS", "path": jl_path, "version": ver, "message": "jlink available"}
    except Exception as exc:
        return {"status": "FAIL", "path": jl_path, "version": None, "message": str(exc)}


def check_wix_toolset() -> dict[str, object]:
    """Verify WiX Toolset for building Windows MSI/EXE installers with jpackage."""
    wix_bin = shutil.which("wix")
    candle_bin = shutil.which("candle")
    light_bin = shutil.which("light")

    # Inspect typical Windows installation directories
    candidate_wix_paths = [
        Path(r"C:\Program Files (x86)\WiX Toolset v3.11\bin"),
        Path(r"C:\Program Files (x86)\WiX Toolset v3.14\bin"),
        Path(r"C:\Program Files\WiX Toolset v4\bin"),
        Path(r"C:\Program Files\WiX Toolset v5\bin"),
    ]

    for cand in candidate_wix_paths:
        if cand.exists():
            c_exe = cand / "candle.exe"
            l_exe = cand / "light.exe"
            w_exe = cand / "wix.exe"
            if c_exe.exists() and l_exe.exists():
                candle_bin = str(c_exe)
                light_bin = str(l_exe)
                break
            if w_exe.exists():
                wix_bin = str(w_exe)
                break

    if candle_bin and light_bin:
        return {
            "status": "PASS",
            "type": "WiX v3",
            "candle": candle_bin,
            "light": light_bin,
            "message": "WiX Toolset v3 detected (ready for jpackage MSI/EXE)",
        }
    if wix_bin:
        return {
            "status": "PASS",
            "type": "WiX v4+",
            "wix": wix_bin,
            "message": "WiX Toolset v4+ detected",
        }

    return {
        "status": "WARN",
        "type": "NOT_DETECTED",
        "message": (
            "WiX Toolset not detected in PATH or standard Program Files. "
            "Note: Required only when generating .msi or .exe installers via jpackage. "
            "Application packaging into self-contained directory (app-image) works without WiX."
        ),
        "remediation": "Install via 'dotnet tool install --global wix' or download WiX v3.11/v3.14 binaries.",
    }


def main() -> int:
    """Run packaging readiness check."""
    jp = check_jpackage()
    jl = check_jlink()
    wix = check_wix_toolset()

    print("=" * 60)
    print("SkillLens Desktop Packaging Readiness Check")
    print("=" * 60)
    print(f"jpackage (JDK 25): [{jp['status']}] {jp.get('path')} (v{jp.get('version')})")
    print(f"jlink (JDK 25):    [{jl['status']}] {jl.get('path')} (v{jl.get('version')})")
    print(f"WiX Toolset:       [{wix['status']}] {wix.get('type')}")
    print(f"  Details:         {wix.get('message')}")
    if wix["status"] == "WARN":
        print(f"  Remediation:     {wix.get('remediation')}")
    print("-" * 60)

    # App-image is fully supported if jpackage and jlink are present
    can_package_app_image = jp["status"] == "PASS" and jl["status"] == "PASS"
    can_build_msi = can_package_app_image and wix["status"] == "PASS"

    print(f"Self-contained app-image capability: [{'READY' if can_package_app_image else 'BLOCKED'}]")
    print(f"Native Windows MSI/EXE installer:    [{'READY' if can_build_msi else 'REQUIRES_WIX'}]")
    print("=" * 60)

    return 0 if can_package_app_image else 1


if __name__ == "__main__":
    sys.exit(main())
