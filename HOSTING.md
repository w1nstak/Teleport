# Хостинг Teleport — iPhone и Android без настройки сервера

Приложения подключаются к **одному облачному URL** (HTTPS). Пользователю на iPhone ничего вводить не нужно.

## Шаг 1. Выберите хостинг

| Вариант | Сложность | Цена | HTTPS |
|---------|-----------|------|-------|
| **VPS** (Timeweb, Selectel, Hetzner) | средняя | от ~300 ₽/мес | через certbot |
| **Render.com** | простая | бесплатный тариф | да |
| **Railway.app** | простая | от $5 | да |
| **Свой VPS + Docker** | простая | от ~300 ₽/мес | nginx + certbot |

Нужен **домен** (например `mysite.ru`) или поддомен хостинга (`teleport.onrender.com`).

---

## Шаг 2. Деплой через Docker (VPS или любой сервер)

### На сервере (Ubuntu)

```bash
# 1. Установите Docker
curl -fsSL https://get.docker.com | sh

# 2. Скопируйте папку teleport на сервер (git clone или scp)
git clone ВАШ_РЕПО /opt/teleport
cd /opt/teleport

# 3. Укажите публичный URL
echo "PUBLIC_URL=https://api.mysite.ru" >> .env

# 4. Запуск
docker compose up -d --build

# 5. Проверка
curl http://127.0.0.1:8765/health
```

### HTTPS с Nginx (обязательно для iPhone)

```bash
sudo apt install nginx certbot python3-certbot-nginx
sudo cp deploy/nginx-teleport.conf /etc/nginx/sites-available/teleport
# Отредактируйте server_name на api.mysite.ru
sudo ln -s /etc/nginx/sites-available/teleport /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d api.mysite.ru
```

В DNS добавьте A-запись: `api.mysite.ru` → IP сервера.

Проверка: `curl https://api.mysite.ru/health`

---

## Шаг 3. Укажите URL в приложениях (один раз)

На **Windows** (перед сборкой IPA/APK):

```powershell
cd teleport
.\setup_public_url.ps1 https://api.mysite.ru
```

Или отредактируйте `public_url.env`:

```
PUBLIC_URL=https://api.mysite.ru
```

и запустите `.\setup_public_url.ps1` без аргументов.

Скрипт обновит:
- `ios/project.yml` — URL для Release-сборки IPA
- `android/local.properties` — URL для APK
- `server/.env` — PUBLIC_URL

---

## Шаг 4. Соберите приложения

**iOS (Mac):**
```bash
cd ios
xcodegen generate
DEVELOPMENT_TEAM=XXX ./build_ipa.sh
```

**Android:**
```powershell
cd android
.\gradlew.bat assembleRelease
```

После установки IPA на iPhone приложение сразу идёт на `https://api.mysite.ru` — **без ввода IP**.

---

## Amvera Cloud (Россия, оплата картой РФ)

**Пошаговая инструкция:** **[AMVERA.md](AMVERA.md)**

Кратко:
1. Создайте проект на [amvera.ru](https://amvera.ru)
2. Загрузите папку `teleport/` (в корне уже есть `amvera.yml`)
3. Включите бесплатный домен `https://teleport.логин.amvera.io`
4. `.\setup_public_url.ps1 https://teleport.логин.amvera.io`
5. Пересоберите IPA/APK

---

## Render.com (без своего VPS)

1. Зарегистрируйтесь на [render.com](https://render.com)
2. **New → Web Service** → подключите репозиторий
3. Настройки:
   - **Root Directory:** `teleport`
   - **Dockerfile Path:** `Dockerfile`
   - **Environment:** `PUBLIC_URL=https://teleport-xxxx.onrender.com`
   - **Disk** (платно): `/data` для SQLite — иначе данные сбросятся при перезапуске
4. После деплоя скопируйте URL и выполните `setup_public_url.ps1`

---

## Railway.app

1. [railway.app](https://railway.app) → New Project → Deploy from GitHub
2. Укажите корень `teleport`, Dockerfile
3. Variables: `PUBLIC_URL=https://ваш-проект.up.railway.app`
4. Volume mount `/data` → `DATA_DIR=/data`
5. `setup_public_url.ps1` с этим URL

---

## Переменные окружения сервера

| Переменная | Описание |
|------------|----------|
| `PORT` | Порт (хостинги часто задают сами, напр. 10000) |
| `DATA_DIR` | Папка для БД и загрузок (`/data` в Docker) |
| `PUBLIC_URL` | `https://api.mysite.ru` — для логов и health |
| `SMS_PROVIDER` | `mock`, `smsru`, `smsc`, `twilio` |

---

## Веб-версия

После деплоя откройте в браузере тот же URL — там и API, и веб-интерфейс:

`https://api.mysite.ru/`

---

## Локальная разработка

- **iOS Debug** в Xcode — автоматически `http://127.0.0.1:8765/`
- **Android эмулятор** — добавьте в `local.properties`: `api.base.url=http://10.0.2.2:8765/`
- **Сервер локально:** `START_SERVER.bat`

---

## Частые проблемы

**iPhone не подключается**
- Нужен **HTTPS** (не http) в Release-сборке
- Проверьте: Safari на iPhone → `https://api.mysite.ru/health`

**WebSocket не работает**
- Nginx должен проксировать `Upgrade` и `Connection` (см. `deploy/nginx-teleport.conf`)

**Данные пропали после перезапуска**
- Подключите volume `/data` или постоянный диск на хостинге
