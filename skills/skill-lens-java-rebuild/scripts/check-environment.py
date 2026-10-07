#!/usr/bin/env python3
"""
Environment verification tool for the SkillLens Java Rebuild project.
Validates JDK 25, compiler, packaging tools (jpackage, jlink),
build systems, Node/npm runtime, Git, and Windows environment parameters.
"""

import json
import os
from pathlib import Path
import platform
import shutil
import subprocess
import sys


def run_cmd(cmd: list[str]) -> tuple[int, str, str]:
    """Execute command safely and capture output."""
    try:
        # On Windows, resolve .cmd or .bat if not direct exe
        executable = shutil.which(cmd[0])
        exec_list = [executable] + cmd[1:] if executable else cmd
        use_shell = sys.platform == "win32" and executable is not None and executable.lower().endswith((".cmd", ".bat"))
        proc = subprocess.run(
            exec_list,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=False,
            shell=use_shell,
            timeout=10,
        )
        return proc.returncode, proc.stdout.strip(), proc.stderr.strip()
    except Exception as exc:
        return -1, "", str(exc)


def check_java() -> dict[str, object]:
    """Inspect Java runtime and compiler."""
    java_bin = shutil.which("java")
    javac_bin = shutil.which("javac")
    code, stdout, stderr = run_cmd(["java", "-version"]) if java_bin else (-1, "", "Not in PATH")
    ver_output = stdout or stderr

    code_c, stdout_c, stderr_c = run_cmd(["javac", "-version"]) if javac_bin else (-1, "", "Not in PATH")
    javac_output = stdout_c or stderr_c

    java_home = os.environ.get("JAVA_HOME", "")
    is_jdk25 = "25." in ver_output or "25." in javac_output

    return {
        "java_path": java_bin or "NOT_FOUND",
        "javac_path": javac_bin or "NOT_FOUND",
        "java_version_raw": ver_output,
        "javac_version_raw": javac_output,
        "java_home": java_home,
        "is_jdk25_detected": is_jdk25,
        "status": "PASS" if (java_bin and javac_bin and is_jdk25) else "WARN",
    }


def check_packaging_tools() -> dict[str, object]:
    """Check jpackage and jlink tools."""
    jpackage_bin = shutil.which("jpackage")
    jlink_bin = shutil.which("jlink")

    # Also inspect standard JDK 25 path if not directly in PATH
    candidate_jdk25 = Path(r"C:\Program Files\Java\jdk-25.0.4\bin")
    if not jpackage_bin and candidate_jdk25.exists():
        cand = candidate_jdk25 / "jpackage.exe"
        if cand.exists():
            jpackage_bin = str(cand)

    if not jlink_bin and candidate_jdk25.exists():
        cand = candidate_jdk25 / "jlink.exe"
        if cand.exists():
            jlink_bin = str(cand)

    code_jp, stdout_jp, stderr_jp = (
        run_cmd([jpackage_bin, "--version"]) if jpackage_bin else (-1, "", "Not found")
    )
    code_jl, stdout_jl, stderr_jl = (
        run_cmd([jlink_bin, "--version"]) if jlink_bin else (-1, "", "Not found")
    )

    return {
        "jpackage_path": jpackage_bin or "NOT_FOUND",
        "jpackage_version": stdout_jp or stderr_jp,
        "jlink_path": jlink_bin or "NOT_FOUND",
        "jlink_version": stdout_jl or stderr_jl,
        "status": "PASS" if (jpackage_bin and jlink_bin) else "WARN",
    }


def check_build_tools() -> dict[str, object]:
    """Check Gradle and build system availability."""
    gradle_bin = shutil.which("gradle")
    code, stdout, stderr = (
        run_cmd(["gradle", "-v"]) if gradle_bin else (-1, "", "gradle executable not found on PATH")
    )
    has_gradlew = Path("gradlew.bat").exists() or Path("gradlew").exists()

    return {
        "gradle_path": gradle_bin or "NOT_FOUND",
        "gradle_version_output": stdout.split("\n")[0] if stdout else stderr,
        "gradlew_wrapper_present": has_gradlew,
        "status": "PASS" if (gradle_bin or has_gradlew) else "PENDING_SCAFFOLD",
        "note": "Gradle wrapper will be supplied when Java project structure is bootstrapped.",
    }


def check_node_toolchain() -> dict[str, object]:
    """Check Node, npm, npx toolchain for skill scripts and frontend tooling."""
    node_bin = shutil.which("node")
    npm_bin = shutil.which("npm")
    npx_bin = shutil.which("npx")

    code_node, out_node, _ = run_cmd(["node", "-v"]) if node_bin else (-1, "", "")
    code_npm, out_npm, _ = run_cmd(["npm", "-v"]) if npm_bin else (-1, "", "")
    code_npx, out_npx, _ = run_cmd(["npx", "-v"]) if npx_bin else (-1, "", "")

    return {
        "node_path": node_bin or "NOT_FOUND",
        "node_version": out_node,
        "npm_path": npm_bin or "NOT_FOUND",
        "npm_version": out_npm,
        "npx_path": npx_bin or "NOT_FOUND",
        "npx_version": out_npx,
        "status": "PASS" if (node_bin and npm_bin and npx_bin) else "WARN",
    }


def check_git() -> dict[str, object]:
    """Check Git availability and configuration."""
    git_bin = shutil.which("git")
    code, out, _ = run_cmd(["git", "--version"]) if git_bin else (-1, "", "")
    is_repo = Path(".git").is_dir()

    return {
        "git_path": git_bin or "NOT_FOUND",
        "git_version": out,
        "is_git_repository": is_repo,
        "status": "PASS" if (git_bin and is_repo) else "WARN",
    }


def main() -> int:
    """Run full environment inspection and output summary."""
    as_json = "--json" in sys.argv

    report = {
        "platform": {
            "system": platform.system(),
            "release": platform.release(),
            "version": platform.version(),
            "machine": platform.machine(),
            "python_version": platform.python_version(),
        },
        "java": check_java(),
        "packaging": check_packaging_tools(),
        "build": check_build_tools(),
        "node": check_node_toolchain(),
        "git": check_git(),
    }

    if as_json:
        print(json.dumps(report, indent=2))
        return 0

    print("=" * 60)
    print("SkillLens Environment Verification Report")
    print("=" * 60)
    print(f"OS Platform:   {report['platform']['system']} {report['platform']['release']} ({report['platform']['machine']})")
    print(f"Python:        {report['platform']['python_version']}")
    print("-" * 60)
    print(f"Java Runtime:  [{report['java']['status']}] {report['java']['java_path']}")
    print(f"  Version:     {report['java']['java_version_raw'].splitlines()[0] if report['java']['java_version_raw'] else 'N/A'}")
    print(f"Java Compiler: [{report['java']['status']}] {report['java']['javac_path']}")
    print(f"JDK 25 Flag:   {report['java']['is_jdk25_detected']}")
    print(f"JAVA_HOME:     {report['java']['java_home']}")
    print("-" * 60)
    print(f"jpackage:      [{report['packaging']['status']}] {report['packaging']['jpackage_path']}")
    print(f"jlink:         [{report['packaging']['status']}] {report['packaging']['jlink_path']}")
    print("-" * 60)
    print(f"Build System:  [{report['build']['status']}]")
    print(f"  Gradle:      {report['build']['gradle_path']}")
    print(f"  Gradlew:     {report['build']['gradlew_wrapper_present']}")
    print(f"  Note:        {report['build']['note']}")
    print("-" * 60)
    print(f"Node Runtime:  [{report['node']['status']}] {report['node']['node_version']} ({report['node']['node_path']})")
    print(f"npm:           {report['node']['npm_version']}")
    print(f"Git:           [{report['git']['status']}] {report['git']['git_version']} (Repo: {report['git']['is_git_repository']})")
    print("=" * 60)
    return 0


if __name__ == "__main__":
    sys.exit(main())
