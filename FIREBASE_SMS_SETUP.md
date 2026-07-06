# СМС через Firebase (как в обычных Android-приложениях)

Teleport отправляет СМС **через Google Firebase**, без SMS.ru и без своего SMS-сервера.

## Настройка (один раз)

### 1. Firebase-проект
1. Откройте https://console.firebase.google.com/
2. **Создать проект** → например `Teleport`
3. Включите **Blaze (pay-as-you-go)** — для Phone Auth нужен платный план (есть бесплатная квота SMS)

### 2. Android-приложение в Firebase
1. **Добавить приложение** → Android
2. Package name: `com.teleport.messenger`
3. Скачайте **`google-services.json`**
4. Положите файл сюда:
   ```
   teleport/android/app/google-services.json
   ```

### 3. Включить Phone Authentication
1. Firebase Console → **Authentication** → **Sign-in method**
2. Включите **Phone**
3. (Опционально) добавьте тестовые номера для разработки без SMS

### 4. Пересоберите APK
```powershell
BUILD_TELEPORT_APK.bat
```

После добавления `google-services.json` в сборке включается `FIREBASE_SMS=true` и СМС уходят с серверов Google.

---

## Как это работает

```
Ввод номера → Firebase Phone Auth → Google отправляет СМС → Ввод кода → вход
```

Если Firebase не настроен, приложение пробует сервер (`sms.ru`) — см. [SMS_SETUP.md](SMS_SETUP.md).

Номера **+7** и **+375** поддерживаются.

---

## Тест без Firebase (разработка)

Если `google-services.json` нет:
1. Запустите `START_SMS_SERVER.bat` (режим `mock`)
2. Код появится в консоли сервера и на экране приложения

---

## Частые проблемы

| Проблема | Решение |
|----------|---------|
| «Firebase не настроен» | Добавьте `google-services.json` и пересоберите |
| SMS не приходит | Проверьте Blaze-план, лимиты в Firebase Console |
| «blocked» / «quota» | Превышен лимит — подождите или добавьте тестовый номер |
| Эмулятор | Используйте тестовый номер из Firebase Console |
