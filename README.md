# Teleport — Android Messenger

Вся папка проекта Teleport. **APK лежит здесь:** `Teleport.apk`

## Быстрый старт

| Действие | Файл |
|----------|------|
| **Установить APK** | `teleport/Teleport.apk` |
| **Собрать APK** | `teleport/BUILD_APK.bat` |
| **Запустить сервер** | `teleport/START_SERVER.bat` |
| **Хостинг Amvera** | `teleport/AMVERA.md` |
| **Хостинг (общее)** | `teleport/HOSTING.md` |

Из корня `neon/` тоже работают: `BUILD_TELEPORT_APK.bat` и `START_TELEPORT.bat`.

## Структура

```
teleport/
  Teleport.apk          ← готовый APK (после сборки)
  BUILD_APK.bat         ← собрать и обновить Teleport.apk
  START_SERVER.bat      ← backend :8765
  android/              ← исходники Android (Kotlin/Compose)
  server/               ← FastAPI backend
  README.md
  SMS_SETUP.md
  YANDEX_SETUP.md
  FIREBASE_SMS_SETUP.md
```

## Сборка вручную

```powershell
cd teleport\android
.\gradlew.bat assembleDebug
copy app\build\outputs\apk\debug\app-debug.apk ..\Teleport.apk
```

## Сервер

```powershell
cd teleport\server
pip install -r requirements.txt
python main.py
```

**Локально:** `http://localhost:8765`  
**iPhone/Android в продакшене:** облачный HTTPS — см. **[HOSTING.md](HOSTING.md)**

```powershell
.\setup_public_url.ps1 https://api.ВАШ-ДОМЕН.ru
```

## Первый запуск

1. Запустите `START_SERVER.bat` (для общения с друзьями)
2. Установите `Teleport.apk` на телефон
3. Зарегистрируйтесь по номеру → чат «Teleport» с приветствием
4. Задайте @username → найдите друга через поиск

## Стек

- Kotlin + Jetpack Compose, Room, Retrofit, WebSocket, WebRTC
- FastAPI + SQLite backend
- Android 10+ (API 29+)
