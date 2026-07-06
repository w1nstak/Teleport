@echo off
title Teleport — сервер
cd /d "%~dp0server"
echo.
echo  ========================================
echo    Teleport Server
echo  ========================================
echo.
echo  http://127.0.0.1:8765
echo  Веб-версия: http://127.0.0.1:8765/
echo  Для телефона в local.properties:
echo  api.base.url=http://IP_ПК:8765/
echo.
python main.py
pause
