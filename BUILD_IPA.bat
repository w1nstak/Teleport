@echo off
chcp 65001 >nul
title Teleport — сборка IPA
echo.
echo  Teleport iOS v1.0.1 — сборка IPA
echo  =================================
echo.
echo  IPA нельзя собрать на Windows. Нужен Mac с Xcode 15+.
echo.
echo  Вариант A — Mac (рекомендуется)
echo  --------------------------------
echo  1. Скопируйте папку teleport на Mac (или клонируйте с Amvera)
echo  2. Терминал:
echo       cd ios
echo       brew install xcodegen
echo       export DEVELOPMENT_TEAM=ВАШ_TEAM_ID
echo       ./build_ipa.sh
echo  3. Файл: teleport\Teleport.ipa
echo.
echo  Вариант B — GitHub Actions (облачный Mac)
echo  -----------------------------------------
echo  1. Залейте репозиторий на GitHub
echo  2. Settings - Secrets - DEVELOPMENT_TEAM = Team ID из Apple Developer
echo  3. Actions - Build iOS IPA - Run workflow
echo  4. Скачайте артефакт Teleport-ipa
echo.
echo  Сервер уже в сборке: https://teleport-w1nst.amvera.io/
echo  Панель владельца: username w1nst
echo.
echo  Подробности: ios\BUILD_IOS.md
echo.
pause
