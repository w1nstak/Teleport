# Teleport на Amvera Cloud

Пошаговая инструкция: сервер в облаке с **бесплатным HTTPS** (`*.amvera.io`), iPhone/Android подключаются сами.

## Что нужно

- Аккаунт на [amvera.ru](https://amvera.ru) (при регистрации ~111 ₽ на тест)
- Папка `teleport/` из этого проекта
- 10–15 минут

---

## Шаг 1. Создайте проект в Amvera

1. Войдите в [cloud.amvera.ru](https://cloud.amvera.ru) (или amvera.ru → личный кабинет).
2. **Создать проект** → имя, например `teleport`.
3. Выберите тариф (от ~170 ₽/мес за приложение).

---

## Шаг 2. Загрузите код

В Amvera в корень репозитория должен попасть **содержимое папки `teleport/`**, не весь `neon/`.

### Вариант A — Git Amvera (рекомендуется)

1. В проекте Amvera откройте **Репозиторий** → скопируйте URL (`https://git.amvera.ru/USER/teleport`).
2. На ПК:

```powershell
cd путь\к\teleport
git init
git add amvera.yml requirements.txt server web android ios ...
git commit -m "Teleport for Amvera"
git remote add amvera https://git.amvera.ru/ВАШ_USER/teleport
git push amvera master
```

> Файл `amvera.yml` уже лежит в `teleport/` — Amvera соберёт проект автоматически.

### Вариант B — Загрузка ZIP

1. Заархивируйте **только** папку `teleport` (с `amvera.yml`, `server/`, `web/`, `requirements.txt`).
2. В интерфейсе Amvera: **Репозиторий** → загрузить файлы.

---

## Шаг 3. Переменные окружения

В проекте Amvera: **Настройки → Переменные окружения** → добавьте:

| Переменная | Значение |
|------------|----------|
| `DATA_DIR` | `/data` |
| `PUBLIC_URL` | `https://teleport.ВАШ_USER.amvera.io` (подставьте свой URL после шага 4) |

`SMS_PROVIDER=mock` — по умолчанию (код SMS в логах). Для боевого SMS см. `SMS_SETUP.md`.

После первого деплоя обновите `PUBLIC_URL` точным адресом и нажмите **Пересобрать** (или сделайте пустой commit).

---

## Шаг 4. Бесплатный домен HTTPS

1. **Настройки → Доменные имена → Добавить доменное имя**
2. Тип подключения: **HTTPS**
3. Тип домена: **Бесплатный домен Amvera**
4. **Применить**

Через 1–2 минуты появится URL вида:

```
https://teleport.ваш-логин.amvera.io
```

Проверка в браузере:

```
https://teleport.ваш-логин.amvera.io/health
```

Должно вернуть `{"ok": true, ...}`.

Веб-мессенджер: тот же URL в браузере (главная страница).

---

## Шаг 5. Пропишите URL в приложениях (один раз)

На Windows:

```powershell
cd teleport
.\setup_public_url.ps1 https://teleport.ваш-логин.amvera.io
```

Или `SETUP_CLOUD.bat` и вставьте URL.

Пересоберите **IPA** (Mac) или **APK** — на iPhone **ничего вводить не нужно**.

---

## Шаг 6. Сборка на Amvera

Если push в Git не запустил сборку автоматически:

**Конфигурация → Собрать**

Логи смотрите в разделе **Сборка**. Успех — статус **«Приложение запущено»**.

### Если 502 / 503

- В `amvera.yml` порт **5000** и `containerPort: 5000` (уже настроено).
- Команда слушает `0.0.0.0`, не `127.0.0.1`.
- В логах нет ошибок `ModuleNotFoundError` — проверьте `requirements.txt` в корне.

---

## Как устроено

| Файл | Назначение |
|------|------------|
| `amvera.yml` | Сборка Python + запуск FastAPI |
| `requirements.txt` | Зависимости в корне (требование Amvera) |
| `server/` | API + WebSocket |
| `web/` | Веб-интерфейс |
| `/data` на сервере | SQLite БД и загрузки (не пропадают при пересборке) |

Конфиг `amvera.yml`:

```yaml
run:
  command: uvicorn --host 0.0.0.0 --port 5000 --app-dir server main:app
  containerPort: 5000
  persistenceMount: /data
```

---

## Обновление

```powershell
cd teleport
git add .
git commit -m "update"
git push amvera master
```

Amvera пересоберёт проект автоматически.

---

## Свой домен (не amvera.io)

1. **Настройки → Доменные имена → Свой домен**
2. У регистратора: **A-запись** на IP из Amvera + **TXT** как в инструкции.
3. Тип подключения: **HTTPS** → сертификат выпустится за 2–3 минуты.
4. `setup_public_url.ps1 https://api.ваш-домен.ru`

---

## Стоимость

- Тестовый баланс при регистрации.
- Минимальный тариф приложения — от ~170 ₽/мес (уточняйте на amvera.ru).
- Бесплатный поддомен `*.amvera.io` и SSL включены.

---

## Краткий чеклист

- [ ] Проект Amvera, в репозитории корень = папка `teleport`
- [ ] Есть `amvera.yml` и `requirements.txt`
- [ ] Сборка успешна, `/health` отвечает
- [ ] Включён бесплатный домен HTTPS
- [ ] `PUBLIC_URL` в переменных окружения
- [ ] `setup_public_url.ps1` + пересборка IPA/APK

Готово — мессенджер работает с iPhone без указания IP сервера.
