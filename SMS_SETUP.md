# СМС-код при входе (как в Telegram)

Teleport отправляет **настоящую СМС** на номер телефона. Приоритет:

1. **Firebase Phone Auth** (рекомендуется) — СМС от Google
2. **Сервер + sms.ru / smsc.ru** — если Firebase нет, но сервер настроен
3. **SIM телефона** — если нет интернет-шлюза
4. **Код на экране** — только если ничего из выше не сработало

---

## Вариант A: Firebase (лучший для Android)

Подробно: [FIREBASE_SMS_SETUP.md](FIREBASE_SMS_SETUP.md)

Кратко:
1. [console.firebase.google.com](https://console.firebase.google.com/) → проект → Android `com.teleport.messenger`
2. Скачайте `google-services.json` → `teleport/android/app/google-services.json`
3. Authentication → **Phone** → включить
4. Тариф **Blaze** (есть бесплатная квота SMS)
5. `BUILD_APK.bat`

После этого СМС приходит **как в обычных приложениях** — без своего SMS-сервера.

---

## Вариант B: Сервер + sms.ru

1. Зарегистрируйтесь на [sms.ru](https://sms.ru/)
2. Скопируйте API ID
3. В `teleport/server/.env`:

```env
SMS_PROVIDER=smsru
SMSRU_API_ID=ваш_api_id
SMSRU_FROM=Teleport
```

4. В `teleport/android/local.properties` укажите IP ПК:

```properties
api.base.url=http://192.168.1.10:8765/
```

5. Запустите `START_SERVER.bat`, пересоберите APK

СМС: `123456 — код для входа в Teleport.`

---

## Вариант C: smsc.ru

```env
SMS_PROVIDER=smsc
SMSC_LOGIN=логин
SMSC_PASSWORD=пароль
```

---

## Тест без реальной СМС

`SMS_PROVIDER=mock` в `server/.env` — код в консоли сервера и на экране приложения.

---

## Частые проблемы

| Проблема | Решение |
|----------|---------|
| СМС не приходит | Настройте Firebase (вариант A) или sms.ru (вариант B) |
| «Firebase не настроен» | Добавьте `google-services.json` |
| Сервер недоступен с телефона | `api.base.url` = IP ПК в Wi‑Fi, не `10.0.2.2` |
| Код только на экране | Нет Firebase и сервер mock / недоступен |
