@echo off
cd /d "%~dp0"

java -Dprofile=win -cp "out;lib/*" service.GameServer