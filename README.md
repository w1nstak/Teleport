# Teleport

Мессенджер в стиле Telegram, но с плоским UI без "Liquid Glass" эффектов.

## Структура проекта

```
Teleport/
├── TeleportApp.swift          — точка входа
├── Theme/
│   └── TeleportTheme.swift    — цвета, стили (без blur/прозрачностей)
├── Models/
│   ├── User.swift
│   ├── Chat.swift
│   └── Message.swift
├── Services/
│   ├── AuthService.swift      — авторизация (заглушка, подключи Firebase/Supabase)
│   ├── ChatService.swift      — загрузка/создание чатов
│   └── MessageService.swift   — сообщения + realtime
├── Views/
│   ├── MainTabView.swift
│   ├── Auth/
│   │   └── LoginView.swift
│   ├── Chats/
│   │   ├── ChatsListView.swift
│   │   └── ContactsView.swift
│   ├── Chat/
│   │   └── ChatView.swift
│   └── Settings/
│       └── SettingsView.swift
└── Components/                — переиспользуемые UI-компоненты
```

## Как запустить

1. Открой проект в Xcode (создай новый iOS App проект и скопируй файлы)
2. Минимальная версия iOS: 16.0
3. Замени заглушки в Services/ на реальный backend (Firebase / Supabase)

## Дизайн-принципы

- Никаких blur / прозрачностей / glass-эффектов
- Сплошные (solid) фоны
- Простые скругления
- Минималистичные анимации
- Плоский, чистый интерфейс

## Roadmap

- [x] MVP: вход, список чатов, экран чата, отправка текста
- [ ] Firebase/Supabase интеграция (realtime)
- [ ] Отправка фото/голосовых
- [ ] Группы и каналы
- [ ] Push-уведомления (APNs)
- [ ] E2E шифрование
- [ ] Звонки (голос/видео)
