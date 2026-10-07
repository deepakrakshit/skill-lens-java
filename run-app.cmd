@echo off
echo Starting SkillLens Desktop Application...
cd /d "%~dp0"
if exist "dist\SkillLens\SkillLens.exe" (
    start "" "dist\SkillLens\SkillLens.exe"
) else (
    echo Packaged executable not found. Running via Maven Wrapper...
    call .\mvnw.cmd spring-boot:run
)
