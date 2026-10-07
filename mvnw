#!/usr/bin/env sh
# SkillLens Maven Wrapper for Unix / Linux / macOS
set -e

if [ -d "/usr/lib/jvm/java-25-openjdk" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-25-openjdk"
fi

DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_CMD="$DIR/tools/apache-maven-3.9.9/bin/mvn"

if [ ! -f "$MAVEN_CMD" ]; then
    if command -v mvn >/dev/null 2>&1; then
        MAVEN_CMD="mvn"
    else
        echo "[ERROR] Maven not found." >&2
        exit 1
    fi
fi

exec "$MAVEN_CMD" "$@"
