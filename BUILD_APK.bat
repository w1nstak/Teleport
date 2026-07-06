@echo off
title Teleport — сборка APK
cd /d "%~dp0android"
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Ошибка сборки.
    pause
    exit /b 1
)
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "%~dp0Teleport.apk" >nul
echo.
echo ========================================
echo   APK готов:
echo   %~dp0Teleport.apk
echo ========================================
pause
