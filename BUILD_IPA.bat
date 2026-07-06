@echo off
chcp 65001 >nul
echo.
echo  Teleport iOS — сборка IPA
echo  ========================
echo.
echo  IPA нельзя собрать на Windows. Нужен Mac с Xcode.
echo.
echo  1. Скопируйте папку teleport\ios на Mac
echo  2. Установите: brew install xcodegen
echo  3. Запустите: build_ipa.sh
echo.
echo  Подробности: ios\BUILD_IOS.md
echo.
pause
