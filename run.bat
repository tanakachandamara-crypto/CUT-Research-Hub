@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo Install desktop Java 17 or newer and make sure java is on PATH.
  pause
  exit /b 1
)
if not exist CUTResearchHub.jar (
  call build.bat
  if errorlevel 1 exit /b 1
)
java -jar CUTResearchHub.jar
if errorlevel 1 pause
