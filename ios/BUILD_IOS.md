# Teleport iOS — сборка IPA

Приложение **само подключается к облаку** — на iPhone ничего настраивать не нужно.

## Перед сборкой

1. Задеплойте сервер → см. **[HOSTING.md](../HOSTING.md)**
2. Укажите URL один раз:

```powershell
cd ..
.\setup_public_url.ps1 https://api.ВАШ-ДОМЕН.ru
```

3. Соберите IPA на Mac:

```bash
cd ios
brew install xcodegen
xcodegen generate
chmod +x build_ipa.sh
DEVELOPMENT_TEAM=ВАШ_TEAM_ID ./build_ipa.sh
```

Файл: `build/ipa/Teleport.ipa`

## Debug (симулятор / разработка)

Xcode Debug использует `http://127.0.0.1:8765/` — запустите `START_SERVER.bat` на ПК.

## Требования

- macOS + Xcode 15+
- HTTPS на хостинге для Release (iPhone)
- Apple ID для установки на свой телефон

Подробный деплой: [HOSTING.md](../HOSTING.md)
