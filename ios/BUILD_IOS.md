# Teleport iOS — сборка IPA (v1.0.1)

Приложение подключается к `https://teleport-w1nst.amvera.io/` — на iPhone ничего настраивать не нужно.

В сборке: панель владельца для **@w1nst** (Настройки → «Панель владельца»).

## Быстрая сборка на Mac

```bash
cd ios
brew install xcodegen
export DEVELOPMENT_TEAM=XXXXXXXXXX   # Team ID из developer.apple.com
chmod +x build_ipa.sh
./build_ipa.sh
```

Готовые файлы:

- `ios/build/ipa/Teleport.ipa`
- `Teleport.ipa` (в корне проекта, рядом с `Teleport.apk`)

## Установка на iPhone

- **Xcode** → Window → Devices → перетащить IPA на устройство  
- или **AltStore** / **Sideloadly** (нужен Apple ID)

## GitHub Actions (без Mac)

1. Репозиторий на GitHub
2. Secret `DEVELOPMENT_TEAM` = ваш Team ID
3. **Actions** → **Build iOS IPA** → **Run workflow**
4. Скачать артефакт **Teleport-ipa**

## Debug (симулятор)

```bash
xcodegen generate
xcodebuild -project Teleport.xcodeproj -scheme Teleport \
  -destination 'platform=iOS Simulator,name=iPhone 15' build
```

Debug использует `http://127.0.0.1:8765/` — запустите `START_SERVER.bat` на ПК.

## Требования

- macOS + Xcode 15+
- Apple ID / Developer (для IPA на реальный iPhone)
- HTTPS на сервере для Release
