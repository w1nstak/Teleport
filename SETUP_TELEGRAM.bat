@echo off
title Teleport — настройка Telegram
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup_telegram.ps1" %*
if %ERRORLEVEL% NEQ 0 pause
