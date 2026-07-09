@echo off
cd /d "%~dp0"

java -Djava.util.logging.config.file=out/logging.properties -cp "out;lib/*" service.GameServer