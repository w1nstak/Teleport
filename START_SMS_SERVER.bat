@echo off
chcp 65001 >nul
cd /d "%~dp0server"

if not exist .env (
    copy .env.example .env >nul
)

pip install -r requirements.txt -q
echo.
echo ========================================
echo  Teleport server :8765
echo  Реальные СМС: Firebase или sms.ru
echo  см. SMS_SETUP.md и FIREBASE_SMS_SETUP.md
echo ========================================
echo.
python main.py
pause
