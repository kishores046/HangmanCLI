@echo off
cd /d "%~dp0"

rmdir /s /q out
mkdir out

dir /s /b src\*.java > sources.txt
javac -cp "src;lib/*" -d out @sources.txt
del sources.txt

copy src\resources\*.properties out\

echo Build completed successfully.