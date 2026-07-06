# Настройка входа через Telegram

Teleport использует официальный **Telegram Login** (OAuth).

## Быстрая настройка

```
SETUP_TELEGRAM.bat
```

## Ручная настройка

### 1. Создайте бота

1. Откройте [@BotFather](https://t.me/BotFather) в Telegram
2. `/newbot` → имя и username (например `TeleportAuthBot`)
3. Скопируйте **токен** (`123456789:ABC...`)

### 2. Привяжите домен

В BotFather:

```
/setdomain
```

Выберите бота и укажите домен, совпадающий с `telegram.auth.origin` (по умолчанию `teleport.app`).

> Нужен **реальный HTTPS-домен**, который вам принадлежит. Без домена OAuth Telegram не работает.

### 3. Укажите данные в проекте

`teleport/android/local.properties`:

```properties
telegram.bot.username=YourBotUsername
telegram.bot.token=123456789:ABCdefGHI...
telegram.auth.origin=https://ваш-домен.ru
```

Или файл `teleport/android/telegram.bot.token` (одна строка — токен).

### 4. Пересоберите APK

```
BUILD_APK.bat
```

## Как работает

1. «Войти через Telegram» → браузер / Telegram OAuth
2. Подтверждение в аккаунте Telegram
3. Callback `com.teleport.messenger://telegram/callback`
4. Проверка подписи `hash` (HMAC-SHA256)
5. Вход в Teleport, @username из Telegram подставляется автоматически

## Частые проблемы

| Проблема | Решение |
|----------|---------|
| «Telegram не настроен» | Запустите `SETUP_TELEGRAM.bat` |
| «Подпись не прошла проверку» | Проверьте токен бота |
| OAuth не открывается | `/setdomain` в BotFather для вашего origin |
| Домена нет | Зарегистрируйте домен или используйте вход по номеру |
