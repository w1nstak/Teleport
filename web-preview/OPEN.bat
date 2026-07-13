@echo off
chcp 65001 >nul
cd /d "%~dp0"

where python >nul 2>&1
if errorlevel 1 (
  echo Python not found — opening file directly...
  start "" "%~dp0index.html"
  exit /b 0
)

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8765" ^| findstr LISTENING') do taskkill /F /PID %%a >nul 2>&1

start "TeleportWebPreview" /min python -m http.server 8765 --bind 127.0.0.1
ping 127.0.0.1 -n 3 >nul
start "" "http://127.0.0.1:8765/"
