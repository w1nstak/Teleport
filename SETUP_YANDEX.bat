@echo off
title Teleport — настройка Яндекс ID
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup_yandex.ps1" %*
if %ERRORLEVEL% NEQ 0 pause
