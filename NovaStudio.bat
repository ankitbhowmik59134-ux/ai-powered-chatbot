@echo off
cd /d "%~dp0"
echo Starting Nova Studio app...
call mvnw.cmd -q javafx:run
if errorlevel 1 pause
