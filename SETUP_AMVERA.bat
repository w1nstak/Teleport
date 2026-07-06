@echo off
chcp 65001 >nul
echo.
echo  Teleport — деплой на Amvera Cloud
echo  ================================
echo.
echo  Подробная инструкция: AMVERA.md
echo.
echo  1. Создайте проект на amvera.ru
echo  2. Загрузите папку teleport (с amvera.yml)
echo  3. Включите бесплатный домен HTTPS
echo  4. Введите URL приложения:
echo.
set /p URL="URL (https://teleport.login.amvera.io): "
if "%URL%"=="" (
  echo Отмена.
  pause
  exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup_public_url.ps1" "%URL%"
echo.
echo  Готово. Пересоберите IPA/APK.
pause
