@echo off
setlocal

cd /d "%~dp0" || exit /b 1

call mvn clean package

if %ERROR LEVEL% neq 0 (
    echo Build failed.
    exit /b %ERRORLEVEL%
)

echo Build completed successfully.