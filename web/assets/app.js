const API = "";
const STORAGE_KEY = "teleport_web_token";
const LOCALE_KEY = "teleport_locale_overrides";

const DEFAULT_STRINGS = {
  chats_title: "Чаты",
  search: "Поиск",
  archive: "Архив",
  read_all: "Прочитать все",
  select_chats: "Выбрать чаты",
  done: "Готово",
  nav_chats: "Чаты",
  nav_contacts: "Контакты",
  nav_profile: "Вы",
  nav_settings: "Настройки",
  nav_calls: "Звонки",
  nav_search: "Поиск",
  message_placeholder: "Сообщение...",
  localization_title: "Локализация",
  localization_section_chats: "Чаты",
  localization_section_nav: "Навигация",
  edit_label: "Изменить надпись",
  original_label: "Оригинал",
  reset: "Сбросить",
  save: "Сохранить",
  cancel: "Отмена",
};

const LOCALIZABLE = [
  { key: "chats_title", section: "localization_section_chats" },
  { key: "search", section: "localization_section_chats" },
  { key: "archive", section: "localization_section_chats" },
  { key: "read_all", section: "localization_section_chats" },
  { key: "select_chats", section: "localization_section_chats" },
  { key: "message_placeholder", section: "localization_section_chats" },
  { key: "nav_chats", section: "localization_section_nav" },
  { key: "nav_profile", section: "localization_section_nav" },
  { key: "nav_settings", section: "localization_section_nav" },
  { key: "nav_search", section: "localization_section_nav" },
];

function loadOverrides() {
  try {
    return JSON.parse(localStorage.getItem(LOCALE_KEY) || "{}");
  } catch {
    return {};
  }
}

function saveOverrides(overrides) {
  localStorage.setItem(LOCALE_KEY, JSON.stringify(overrides));
}

function str(key) {
  const overrides = loadOverrides();
  return overrides[key] || DEFAULT_STRINGS[key] || key;
}

function applyLabels() {
  const set = (id, fn) => { const el = $(id); if (el) fn(el); };
  set("label-chats-title", (el) => { el.textContent = str("chats_title"); });
  set("label-nav-chats", (el) => { el.textContent = str("nav_chats"); });
  set("label-nav-profile", (el) => { el.textContent = str("nav_profile"); });
  set("label-nav-settings", (el) => { el.textContent = str("nav_settings"); });
  set("menu-search", (el) => { el.textContent = str("search"); });
  set("menu-archive", (el) => { el.textContent = str("archive"); });
  set("menu-read-all", (el) => { el.textContent = str("read_all"); });
  set("menu-select", (el) => { el.textContent = str("select_chats"); });
  set("label-locale-title", (el) => { el.textContent = str("localization_title"); });
  set("label-edit", (el) => { el.textContent = str("edit_label"); });
  set("locale-reset", (el) => { el.textContent = str("reset"); });
  set("edit-cancel", (el) => { el.textContent = str("cancel"); });
  set("edit-save", (el) => { el.textContent = str("save"); });
  set("message-input", (el) => { el.placeholder = str("message_placeholder"); });
  set("btn-nav-search", (el) => { el.title = str("nav_search"); });
}

function renderLocaleList() {
  const box = $("locale-list");
  box.innerHTML = "";
  const overrides = loadOverrides();
  let lastSection = "";
  LOCALIZABLE.forEach((entry) => {
    if (entry.section !== lastSection) {
      lastSection = entry.section;
      const sec = document.createElement("div");
      sec.className = "locale-section";
      sec.textContent = str(entry.section);
      box.appendChild(sec);
      const card = document.createElement("div");
      card.style.background = "var(--card)";
      card.style.margin = "0 16px 8px";
      card.style.borderRadius = "12px";
      card.dataset.section = entry.section;
      card.className = "locale-card";
      box.appendChild(card);
    }
    const card = box.querySelector(`.locale-card[data-section="${entry.section}"]`);
    const row = document.createElement("div");
    row.className = "locale-row";
    row.innerHTML = `<span class="orig">${DEFAULT_STRINGS[entry.key]}</span><span class="custom">${overrides[entry.key] || DEFAULT_STRINGS[entry.key]}</span>`;
    row.onclick = () => openEditDialog(entry.key);
    card.appendChild(row);
  });
}

let editingKey = null;

function openEditDialog(key) {
  editingKey = key;
  $("edit-original").textContent = `${str("original_label")}: ${DEFAULT_STRINGS[key]}`;
  $("edit-input").value = loadOverrides()[key] || DEFAULT_STRINGS[key];
  $("edit-overlay").classList.remove("hidden");
}

function closeEditDialog() {
  editingKey = null;
  $("edit-overlay").classList.add("hidden");
}

const state = {
  token: localStorage.getItem(STORAGE_KEY),
  user: null,
  isOwner: false,
  chats: [],
  currentChatId: null,
  currentPeer: null,
  messages: [],
  ws: null,
  lastSync: 0,
};

const $ = (id) => document.getElementById(id);

function show(view) {
  $("view-auth").classList.toggle("hidden", view !== "auth");
  $("view-main").classList.toggle("hidden", view !== "main");
}

function showAuthError(msg) {
  const el = $("auth-error");
  if (!msg) {
    el.classList.add("hidden");
    el.textContent = "";
    return;
  }
  el.textContent = msg;
  el.classList.remove("hidden");
}

async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  const res = await fetch(API + path, { ...options, headers });
  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = { detail: text };
  }
  if (!res.ok) {
    const msg = data?.detail || data?.message || `Ошибка ${res.status}`;
    throw new Error(typeof msg === "string" ? msg : JSON.stringify(msg));
  }
  return data;
}

function initials(name) {
  return (name || "?")
    .split(/\s+/)
    .map((w) => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
}

function formatTime(ts) {
  const d = new Date(ts);
  return d.toLocaleTimeString("ru-RU", { hour: "2-digit", minute: "2-digit" });
}

function setMe(user) {
  state.user = user;
  $("me-name").textContent = user.displayName || "—";
  $("me-username").textContent = user.username ? `@${user.username}` : "";
  $("me-avatar").textContent = initials(user.displayName);
  $("profile-name").textContent = user.displayName || "—";
  $("profile-username").textContent = user.username ? `@${user.username}` : "";
  $("profile-avatar").textContent = initials(user.displayName);
}

async function refreshOwnerStatus() {
  const btn = $("web-btn-admin");
  if (!state.token) {
    state.isOwner = false;
    if (btn) {
      btn.classList.add("hidden");
      btn.classList.remove("admin-row");
    }
    return;
  }
  try {
    const data = await api("/admin/check");
    state.isOwner = !!data?.isOwner;
    if (btn) {
      btn.classList.toggle("hidden", !state.isOwner);
      btn.classList.toggle("admin-row", state.isOwner);
    }
  } catch {
    state.isOwner = false;
    if (btn) btn.classList.add("hidden");
  }
}

function renderAdminStats(stats) {
  const box = $("admin-stats");
  if (!box) return;
  const rows = [
    ["Пользователей", stats.usersTotal],
    ["Сообщений всего", stats.messagesTotal],
    ["Сообщений сегодня", stats.messagesToday],
    ["Чатов", stats.chatsTotal],
    ["Аккаунтов", stats.accountsTotal],
    ["Онлайн сейчас", stats.onlineNow],
    ["WebSocket", stats.wsConnections],
  ];
  if (stats.lastMessageAt) {
    rows.push(["Последнее сообщение", new Date(stats.lastMessageAt).toLocaleString("ru-RU")]);
  }
  if (stats.publicUrl) {
    rows.push(["Сервер", stats.publicUrl]);
  }
  box.innerHTML = rows
    .map(
      ([label, value]) =>
        `<div class="admin-stat"><div class="admin-stat-label">${escapeHtml(label)}</div><div class="admin-stat-value">${escapeHtml(String(value))}</div></div>`,
    )
    .join("");
}

async function loadAdminStats() {
  const box = $("admin-stats");
  if (!state.isOwner) return;
  if (box) box.innerHTML = '<p class="panel-hint">Загрузка…</p>';
  try {
    const stats = await api("/admin/stats");
    renderAdminStats(stats);
  } catch (err) {
    if (box) box.innerHTML = `<p class="panel-hint" style="color:var(--danger)">${escapeHtml(err.message)}</p>`;
  }
}

async function openAdminPanel() {
  if (!state.isOwner) return;
  $("admin-modal")?.classList.remove("hidden");
  await loadAdminStats();
}

function showSidePanel(panel) {
  const isChats = panel === "chats";
  $("chat-list").classList.toggle("hidden", !isChats);
  $("search-wrap").classList.toggle("hidden", !isChats);
  $("chats-header").classList.toggle("hidden", !isChats);
  $("panel-contacts").classList.toggle("hidden", panel !== "contacts");
  $("panel-settings").classList.toggle("hidden", panel !== "settings");
  $("panel-profile").classList.toggle("hidden", panel !== "profile");
  $("panel-calls").classList.toggle("hidden", panel !== "calls");
  document.querySelectorAll(".tab-item").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.panel === panel);
  });
}

function renderChats() {
  const list = $("chat-list");
  list.innerHTML = "";
  if (!state.chats.length) {
    list.innerHTML = '<p style="padding:16px;color:var(--muted);font-size:14px">Нет чатов. Найдите пользователя выше.</p>';
    return;
  }
  state.chats.forEach((chat) => {
    const div = document.createElement("div");
    div.className = "chat-item" + (chat.chatId === state.currentChatId ? " active" : "");
    div.innerHTML = `
      <div class="avatar sm">${initials(chat.title)}</div>
      <div>
        <div class="chat-item-title">${escapeHtml(chat.title)}</div>
        <div class="chat-item-sub">${chat.type === "PRIVATE" ? "личный чат" : chat.type}</div>
      </div>`;
    div.onclick = () => openChatById(chat.chatId, chat.title);
    list.appendChild(div);
  });
}

function escapeHtml(s) {
  return String(s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function renderMessages() {
  const box = $("messages");
  box.innerHTML = "";
  state.messages.forEach((m) => {
    const mine = m.senderId === state.user?.id;
    const div = document.createElement("div");
    div.className = `bubble ${mine ? "me" : "them"}`;
    div.dataset.id = m.id;
    div.innerHTML = `${escapeHtml(m.text)}<time>${formatTime(m.createdAt)}</time>`;
    box.appendChild(div);
  });
  box.scrollTop = box.scrollHeight;
}

function appendMessage(m) {
  if (m.chatId !== state.currentChatId) return;
  if (state.messages.some((x) => x.id === m.id)) return;
  state.messages.push(m);
  const box = $("messages");
  const mine = m.senderId === state.user?.id;
  const div = document.createElement("div");
  div.className = `bubble ${mine ? "me" : "them"}`;
  div.innerHTML = `${escapeHtml(m.text)}<time>${formatTime(m.createdAt)}</time>`;
  box.appendChild(div);
  box.scrollTop = box.scrollHeight;
}

async function loadChats() {
  state.chats = await api("/chats");
  renderChats();
}

async function loadMessages(chatId) {
  const data = await api(`/chats/${chatId}/messages?since=0&limit=300`);
  state.messages = data.messages || [];
  renderMessages();
}

async function openChatById(chatId, title, peer) {
  state.currentChatId = chatId;
  state.currentPeer = peer || null;
  $("empty-chat").classList.add("hidden");
  $("chat-active").classList.remove("hidden");
  $("chat-title").textContent = title || "Чат";
  $("chat-sub").textContent = peer?.username ? `@${peer.username}` : "личный чат";
  $("peer-avatar").textContent = initials(title);
  document.querySelector(".main-view")?.classList.add("chat-open");
  renderChats();
  await loadMessages(chatId);
}

async function openChatWithUser(userId) {
  const data = await api("/chats/open", {
    method: "POST",
    body: JSON.stringify({ otherUserId: userId }),
  });
  await loadChats();
  await openChatById(data.chatId, data.peer?.displayName || data.title, data.peer);
  $("search-results").classList.add("hidden");
  $("search-users").value = "";
}

function connectWs() {
  if (state.ws) {
    state.ws.close();
    state.ws = null;
  }
  if (!state.token) return;
  const proto = location.protocol === "https:" ? "wss:" : "ws:";
  const ws = new WebSocket(`${proto}//${location.host}/ws?token=${encodeURIComponent(state.token)}`);
  state.ws = ws;
  ws.onmessage = (ev) => {
    try {
      const data = JSON.parse(ev.data);
      if (data.event === "message" && data.payload) {
        appendMessage(data.payload);
        if (data.payload.chatId && !state.chats.find((c) => c.chatId === data.payload.chatId)) {
          loadChats();
        }
      }
      if (data.event === "message_updated" && data.payload) {
        const idx = state.messages.findIndex((m) => m.id === data.payload.id);
        if (idx >= 0) {
          state.messages[idx] = data.payload;
          renderMessages();
        }
      }
    } catch (_) {}
  };
  ws.onclose = () => {
    if (state.token) setTimeout(connectWs, 3000);
  };
}

async function afterLogin(token) {
  state.token = token;
  localStorage.setItem(STORAGE_KEY, token);
  const me = await api("/users/me");
  setMe(me);
  await refreshOwnerStatus();
  applyLabels();
  show("main");
  showAuthError("");
  await loadChats();
  connectWs();
}

async function tryRestoreSession() {
  if (!state.token) {
    show("auth");
    return;
  }
  try {
    await afterLogin(state.token);
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    state.token = null;
    show("auth");
  }
}

async function sendMessage(text) {
  if (!state.currentChatId || !text.trim()) return;
  const msg = await api("/messages/send", {
    method: "POST",
    body: JSON.stringify({
      chatId: state.currentChatId,
      type: "TEXT",
      text: text.trim(),
    }),
  });
  appendMessage(msg);
}

// Tabs
document.querySelectorAll(".segment").forEach((tab) => {
  tab.addEventListener("click", () => {
    document.querySelectorAll(".segment").forEach((t) => t.classList.remove("active"));
    tab.classList.add("active");
    const name = tab.dataset.tab;
    $("form-login").classList.toggle("hidden", name !== "login");
    $("form-register").classList.toggle("hidden", name !== "register");
    showAuthError("");
  });
});

$("form-login").addEventListener("submit", async (e) => {
  e.preventDefault();
  showAuthError("");
  try {
    const username = $("login-username").value.trim().replace(/^@/, "");
    const password = $("login-password").value;
    const res = await api("/auth/login/username", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
    await afterLogin(res.token);
  } catch (err) {
    showAuthError(err.message);
  }
});

$("form-register").addEventListener("submit", async (e) => {
  e.preventDefault();
  showAuthError("");
  try {
    const res = await api("/auth/register/web", {
      method: "POST",
      body: JSON.stringify({
        displayName: $("reg-name").value.trim(),
        username: $("reg-username").value.trim().replace(/^@/, ""),
        password: $("reg-password").value,
      }),
    });
    await afterLogin(res.token);
  } catch (err) {
    showAuthError(err.message);
  }
});

$("btn-logout").addEventListener("click", () => {
  if (state.ws) state.ws.close();
  localStorage.removeItem(STORAGE_KEY);
  state.token = null;
  state.user = null;
  state.chats = [];
  state.currentChatId = null;
  state.messages = [];
  show("auth");
  document.querySelector(".main-view")?.classList.remove("chat-open");
});

$("composer").addEventListener("submit", async (e) => {
  e.preventDefault();
  const input = $("message-input");
  const text = input.value;
  input.value = "";
  try {
    await sendMessage(text);
  } catch (err) {
    alert(err.message);
  }
});

let searchTimer;
$("search-users").addEventListener("input", () => {
  clearTimeout(searchTimer);
  const q = $("search-users").value.trim();
  const box = $("search-results");
  if (q.length < 1) {
    box.classList.add("hidden");
    return;
  }
  searchTimer = setTimeout(async () => {
    try {
      const users = await api(`/users/search?q=${encodeURIComponent(q)}`);
      box.innerHTML = "";
      if (!users.length) {
        box.innerHTML = '<div class="search-item"><span style="color:var(--muted)">Никого не найдено</span></div>';
      } else {
        users.forEach((u) => {
          const div = document.createElement("div");
          div.className = "search-item";
          div.innerHTML = `
            <div class="avatar sm">${initials(u.displayName)}</div>
            <div>
              <div class="chat-item-title">${escapeHtml(u.displayName)}</div>
              <div class="chat-item-sub">${u.username ? "@" + escapeHtml(u.username) : u.id.slice(0, 8)}</div>
            </div>`;
          div.onclick = () => openChatWithUser(u.id);
          box.appendChild(div);
        });
      }
      box.classList.remove("hidden");
    } catch (_) {
      box.classList.add("hidden");
    }
  }, 300);
});

$("btn-back").addEventListener("click", () => {
  document.querySelector(".main-view")?.classList.remove("chat-open");
  $("chat-active").classList.add("hidden");
  $("empty-chat").classList.remove("hidden");
  state.currentChatId = null;
  renderChats();
});

$("btn-chats-menu").addEventListener("click", () => {
  $("menu-overlay").classList.remove("hidden");
});

$("menu-overlay").addEventListener("click", (e) => {
  if (e.target === $("menu-overlay")) $("menu-overlay").classList.add("hidden");
});

document.querySelectorAll(".sheet-item").forEach((item) => {
  item.addEventListener("click", () => {
    $("menu-overlay").classList.add("hidden");
    const action = item.dataset.action;
    if (action === "search") $("search-users").focus();
    if (action === "read-all") alert("Все чаты отмечены прочитанными");
    if (action === "select") alert("Режим выбора чатов — в веб-версии скоро");
    if (action === "archive") alert("Архив — откройте в приложении Android");
  });
});

$("btn-localization").addEventListener("click", () => {
  renderLocaleList();
  $("locale-modal").classList.remove("hidden");
});

$("web-btn-locale")?.addEventListener("click", () => {
  renderLocaleList();
  $("locale-modal").classList.remove("hidden");
});

$("web-btn-admin")?.addEventListener("click", () => openAdminPanel());
$("admin-close")?.addEventListener("click", () => $("admin-modal")?.classList.add("hidden"));
$("admin-refresh")?.addEventListener("click", () => loadAdminStats());
$("admin-modal")?.addEventListener("click", (e) => {
  if (e.target === $("admin-modal")) $("admin-modal").classList.add("hidden");
});

$("web-btn-logout2")?.addEventListener("click", () => $("btn-logout").click());

document.querySelectorAll(".tab-item[data-panel]").forEach((btn) => {
  btn.addEventListener("click", () => showSidePanel(btn.dataset.panel));
});

$("locale-close").addEventListener("click", () => $("locale-modal").classList.add("hidden"));
$("locale-modal").addEventListener("click", (e) => {
  if (e.target === $("locale-modal")) $("locale-modal").classList.add("hidden");
});

$("locale-reset").addEventListener("click", () => {
  saveOverrides({});
  applyLabels();
  renderLocaleList();
});

$("edit-cancel").addEventListener("click", closeEditDialog);
$("edit-save").addEventListener("click", () => {
  if (!editingKey) return;
  const val = $("edit-input").value.trim();
  const overrides = loadOverrides();
  if (!val || val === DEFAULT_STRINGS[editingKey]) delete overrides[editingKey];
  else overrides[editingKey] = val;
  saveOverrides(overrides);
  applyLabels();
  renderLocaleList();
  closeEditDialog();
});

$("btn-nav-search")?.addEventListener?.("click", () => $("search-users")?.focus());

applyLabels();
showSidePanel("chats");
tryRestoreSession().catch(() => show("auth"));
