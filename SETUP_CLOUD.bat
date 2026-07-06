@echo off
chcp 65001 >nul
echo.
echo  Teleport — настройка облачного сервера
echo  =====================================
echo.
echo  1. Задеплойте сервер (см. HOSTING.md)
echo  2. Введите ваш HTTPS URL:
echo.
set /p URL="URL (например https://api.mysite.ru): "
if "%URL%"=="" (
  echo Отмена.
  pause
  exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup_public_url.ps1" "%URL%"
echo.
echo  Готово. Пересоберите IPA/APK.
echo  Инструкция: HOSTING.md
pause
