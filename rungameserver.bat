@echo off
setlocal

cd /d "%~dp0" || exit /b 1

java ^
-Djava.util.logging.config.file=target\classes\logging.properties ^
-cp "target\classes;target\dependency\*" ^
service.GameTcpServer

if %ERROR LEVEL% neq 0 (
    echo Server exited with an error.
    exit /b %ERROR LEVEL%
)