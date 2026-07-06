# Настройка входа через Яндекс ID

Teleport использует официальный **Yandex Auth SDK** (`com.yandex.android:authsdk:3.1.3`).

## Быстрая настройка (рекомендуется)

Запустите из папки `teleport/`:

```
SETUP_YANDEX.bat
```

Скрипт покажет SHA256 отпечаток, откроет oauth.yandex.ru, попросит Client ID и пересоберёт APK.

Или с Client ID сразу:

```
SETUP_YANDEX.bat ваш_client_id
```

## Ручная настройка

### 1. Зарегистрируйте приложение

Откройте [oauth.yandex.ru → Создать приложение](https://oauth.yandex.ru/client/new/id/)

| Поле | Значение |
|------|----------|
| Тип | **Для авторизации пользователей** |
| Платформа | **Android-приложение** |
| Имя пакета | `com.teleport.messenger` |
| SHA256 Fingerprints | см. ниже |

**SHA256 (debug-сборка, этот ПК):**

```
E243B8E297A2AF3C0FE9B498F3DB78F3A6A2B7C78290880AF48CA70888566D5A
```

> В консоли Яндекса — **только заглавные буквы, без двоеточий**.

Права доступа: `login:info`, `login:email`.

Redirect URI **не нужен** — SDK использует схему `yx{CLIENT_ID}://auth/finish`.

### 2. Укажите Client ID в проекте

Любой из способов:

**A.** Файл `teleport/android/yandex.client.id` (одна строка — Client ID)

**B.** В `teleport/android/local.properties`:

```properties
yandex.client.id=ВАШ_CLIENT_ID
```

**C.** Переменная окружения `YANDEX_CLIENT_ID` при сборке

### 3. Пересоберите APK

```
BUILD_APK.bat
```

APK: `teleport/Teleport.apk`

## Как работает

1. «Войти через Яндекс ID» → экран Яндекс ID (приложение Яндекса, Chrome Tab или WebView)
2. OAuth-токен → профиль через `https://login.yandex.ru/info`
3. Аккаунт сохраняется локально (`yandex:{id}`)

## Отладка

| Проблема | Решение |
|----------|---------|
| Кнопки нет / ошибка `yandex.client.id` | Запустите `SETUP_YANDEX.bat`, пересоберите APK |
| `invalid_client` / отказ OAuth | Проверьте пакет `com.teleport.messenger` и SHA256 в oauth.yandex.ru |
| Release-сборка не работает | Добавьте SHA256 **release**-ключа в oauth.yandex.ru |

Получить SHA256 debug-ключа:

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -alias androiddebugkey -keystore "$env:USERPROFILE\.android\debug.keystore" -storepass android
```
