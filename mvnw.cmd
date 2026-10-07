@echo off
rem SkillLens Maven Wrapper for Windows (Java 25 target)
setlocal

rem Detect Java 25 if JAVA_HOME is not already JDK 25
if exist "C:\Program Files\Java\jdk-25.0.4" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4"
)

rem Path to local maven installation
set "MAVEN_CMD=%~dp0tools\apache-maven-3.9.9\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    rem Check if mvn is available on system PATH
    where mvn >nul 2>nul
    if %errorlevel% equ 0 (
        set "MAVEN_CMD=mvn"
    ) else (
        echo [ERROR] Maven not found. Expected at %~dp0tools\apache-maven-3.9.9\bin\mvn.cmd
        exit /b 1
    )
)

call "%MAVEN_CMD%" %*
exit /b %errorlevel%
