@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
  echo Install JDK 17 or newer and make sure javac is on PATH.
  pause
  exit /b 1
)
if not exist build\classes mkdir build\classes
javac --release 17 -encoding UTF-8 -d build\classes src\App.java src\core\*.java src\model\*.java src\service\*.java src\gui\*.java
if errorlevel 1 exit /b 1
jar --create --file CUTResearchHub.jar --main-class App -C build\classes .
if errorlevel 1 exit /b 1
echo Build complete. Run run.bat to start CUT Research Hub.
