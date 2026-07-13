"""Teleport Messenger Backend — REST + WebSocket + SQLite."""
from __future__ import annotations

import asyncio
import hashlib
import json
import os
import shutil
import time
import uuid
from datetime import datetime
from pathlib import Path
from typing import Optional

from fastapi import FastAPI, File, Header, HTTPException, UploadFile, WebSocket, WebSocketDisconnect
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

try:
    from dotenv import load_dotenv

    load_dotenv()
except ImportError:
    pass

# Load dotenv before config paths are read
from config import UPLOAD_DIR, WEB_DIR, PORT, HOST, PUBLIC_URL, OWNER_USERNAME  # noqa: E402

from database import connect, init_db
from sms_service import generate_code, normalize_phone, send_sms, sms_is_configured, sms_provider

app = FastAPI(title="Teleport API", version="2.0.0")

UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
WEB_ASSETS = WEB_DIR / "assets"

users_db: dict[str, dict] = {}
accounts_db: dict[str, dict] = {}
tokens_db: dict[str, str] = {}
sms_codes_db: dict[str, dict] = {}
ws_clients: dict[str, WebSocket] = {}

SMS_CODE_TTL_SEC = 300
SMS_RESEND_COOLDOWN_SEC = 60


@app.on_event("startup")
def startup() -> None:
    init_db()
    load_state()
    if PUBLIC_URL:
        print(f"Teleport public URL: {PUBLIC_URL}")


# ---------- models ----------


class AuthRequest(BaseModel):
    phone: str
    password: str
    displayName: Optional[str] = None


class AuthResponse(BaseModel):
    token: str
    userId: str
    accountId: str
    phone: Optional[str] = None
    displayName: Optional[str] = None
    username: Optional[str] = None


class UsernameLoginRequest(BaseModel):
    username: str
    password: str


class WebRegisterRequest(BaseModel):
    username: str
    password: str
    displayName: str


class QrAuthRequest(BaseModel):
    qrToken: str
    deviceName: str


class SendMessageRequest(BaseModel):
    id: Optional[str] = None
    chatId: str
    type: str
    text: str = ""
    mediaUri: Optional[str] = None
    replyToId: Optional[str] = None
    forwardFromId: Optional[str] = None


class EditMessageRequest(BaseModel):
    text: str


class ForwardMessageRequest(BaseModel):
    toChatId: str


class FcmTokenRequest(BaseModel):
    token: str


class SmsSendRequest(BaseModel):
    phone: str


class SmsVerifyRequest(BaseModel):
    phone: str
    code: str


class SmsSendResponse(BaseModel):
    ok: bool
    devCode: Optional[str] = None
    retryAfter: int = 60


class SmsVerifyResponse(BaseModel):
    ok: bool


class OpenChatRequest(BaseModel):
    otherUserId: str


class UpdateProfileRequest(BaseModel):
    displayName: Optional[str] = None
    username: Optional[str] = None
    bio: Optional[str] = None


# ---------- helpers ----------


def private_chat_id(user_a: str, user_b: str) -> str:
    a, b = sorted([user_a, user_b])
    return f"p_{a}_{b}"


def user_to_dict(row) -> dict:
    return {
        "id": row["id"],
        "accountId": row["account_id"],
        "displayName": row["display_name"],
        "username": row["username"],
        "bio": row["bio"] or "",
        "isOnline": bool(row["is_online"]),
        "lastSeen": row["last_seen"] or 0,
        "isPremium": bool(row["is_premium"]),
    }


def load_state() -> None:
    conn = connect()
    users_db.clear()
    accounts_db.clear()
    tokens_db.clear()
    for row in conn.execute("SELECT * FROM users"):
        users_db[row["id"]] = user_to_dict(row)
    for row in conn.execute("SELECT * FROM accounts"):
        accounts_db[row["id"]] = {"phone": row["phone"], "password_hash": row["password_hash"]}
    for row in conn.execute("SELECT * FROM auth_tokens"):
        tokens_db[row["token"]] = row["user_id"]
    conn.close()


def persist_user(user: dict) -> None:
    users_db[user["id"]] = user
    conn = connect()
    conn.execute(
        """
        INSERT OR REPLACE INTO users
        (id, account_id, display_name, username, bio, is_online, last_seen, is_premium)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            user["id"],
            user["accountId"],
            user["displayName"],
            user.get("username"),
            user.get("bio", ""),
            1 if user.get("isOnline") else 0,
            user.get("lastSeen", 0),
            1 if user.get("isPremium") else 0,
        ),
    )
    conn.commit()
    conn.close()


def persist_account(account_id: str, phone: str, password_hash: str) -> None:
    accounts_db[account_id] = {"phone": phone, "password_hash": password_hash}
    conn = connect()
    conn.execute(
        "INSERT OR REPLACE INTO accounts (id, phone, password_hash) VALUES (?, ?, ?)",
        (account_id, phone, password_hash),
    )
    conn.commit()
    conn.close()


def persist_token(token: str, user_id: str) -> None:
    tokens_db[token] = user_id
    conn = connect()
    conn.execute(
        "INSERT OR REPLACE INTO auth_tokens (token, user_id) VALUES (?, ?)",
        (token, user_id),
    )
    conn.commit()
    conn.close()


def ensure_private_chat(user_id: str, other_id: str) -> dict:
    if other_id not in users_db:
        raise HTTPException(404, "User not found")
    chat_id = private_chat_id(user_id, other_id)
    title = users_db[other_id]["displayName"]
    now = int(datetime.now().timestamp() * 1000)
    conn = connect()
    conn.execute(
        "INSERT OR IGNORE INTO chats (id, type, title, created_at) VALUES (?, 'PRIVATE', ?, ?)",
        (chat_id, title, now),
    )
    for member in (user_id, other_id):
        conn.execute(
            "INSERT OR IGNORE INTO chat_members (chat_id, user_id) VALUES (?, ?)",
            (chat_id, member),
        )
    conn.commit()
    conn.close()
    return {"chatId": chat_id, "title": title, "type": "PRIVATE", "members": [user_id, other_id]}


def get_chat_members(chat_id: str) -> list[str]:
    conn = connect()
    rows = conn.execute("SELECT user_id FROM chat_members WHERE chat_id = ?", (chat_id,)).fetchall()
    conn.close()
    return [r["user_id"] for r in rows]


def user_is_chat_member(user_id: str, chat_id: str) -> bool:
    members = get_chat_members(chat_id)
    return user_id in members if members else True


def get_user_chat_ids(user_id: str) -> list[str]:
    conn = connect()
    rows = conn.execute("SELECT chat_id FROM chat_members WHERE user_id = ?", (user_id,)).fetchall()
    conn.close()
    return [r["chat_id"] for r in rows]


async def notify_chat(chat_id: str, payload: dict, exclude_user: Optional[str] = None) -> None:
    data = json.dumps(payload, ensure_ascii=False)
    dead: list[str] = []
    for uid in get_chat_members(chat_id):
        if exclude_user and uid == exclude_user:
            continue
        ws = ws_clients.get(uid)
        if ws:
            try:
                await ws.send_text(data)
            except Exception:
                dead.append(uid)
        if uid != exclude_user:
            body = payload.get("payload", {}).get("text") or "Новое сообщение"
            await push_fcm(uid, "Teleport", body)
    for uid in dead:
        ws_clients.pop(uid, None)


# ---------- helpers (messages) ----------


def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()


def find_user_by_username(username: str) -> Optional[dict]:
    key = username.lstrip("@").lower()
    for u in users_db.values():
        if u.get("username", "").lower() == key:
            return u
    return None


def auth_response(user_id: str, account_id: str, token: str) -> AuthResponse:
    user = users_db[user_id]
    acc = accounts_db.get(account_id, {})
    return AuthResponse(
        token=token,
        userId=user_id,
        accountId=account_id,
        phone=acc.get("phone"),
        displayName=user.get("displayName"),
        username=user.get("username"),
    )


def auth_user_id(authorization: Optional[str]) -> str:
    if not authorization:
        raise HTTPException(401, "Unauthorized")
    token = authorization.removeprefix("Bearer ").strip()
    user_id = tokens_db.get(token)
    if not user_id:
        conn = connect()
        row = conn.execute("SELECT user_id FROM auth_tokens WHERE token = ?", (token,)).fetchone()
        conn.close()
        if row:
            user_id = row["user_id"]
            tokens_db[token] = user_id
    if not user_id:
        raise HTTPException(401, "Invalid token")
    return user_id


def user_record(user_id: str) -> Optional[dict]:
    if user_id in users_db:
        return users_db[user_id]
    conn = connect()
    row = conn.execute("SELECT * FROM users WHERE id = ?", (user_id,)).fetchone()
    conn.close()
    if not row:
        return None
    return user_to_dict(row)


def is_owner_user(user_id: str) -> bool:
    user = user_record(user_id)
    if not user:
        return False
    username = (user.get("username") or "").strip().lstrip("@").lower()
    return username == OWNER_USERNAME


def require_owner(authorization: Optional[str]) -> str:
    user_id = auth_user_id(authorization)
    if not is_owner_user(user_id):
        raise HTTPException(403, "Owner access only")
    return user_id


def row_to_msg(row) -> dict:
    return {
        "id": row["id"],
        "chatId": row["chat_id"],
        "senderId": row["sender_id"],
        "type": row["type"],
        "text": row["text"] or "",
        "mediaUri": row["media_uri"],
        "replyToId": row["reply_to_id"],
        "forwardFromId": row["forward_from_id"],
        "isEdited": bool(row["is_edited"]),
        "editedAt": row["edited_at"],
        "createdAt": row["created_at"],
    }


def save_message(msg: dict) -> dict:
    conn = connect()
    conn.execute(
        """
        INSERT OR REPLACE INTO messages
        (id, chat_id, sender_id, type, text, media_uri, reply_to_id, forward_from_id, is_edited, edited_at, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            msg["id"],
            msg["chatId"],
            msg["senderId"],
            msg["type"],
            msg.get("text", ""),
            msg.get("mediaUri"),
            msg.get("replyToId"),
            msg.get("forwardFromId"),
            1 if msg.get("isEdited") else 0,
            msg.get("editedAt"),
            msg["createdAt"],
        ),
    )
    conn.commit()
    conn.close()
    return msg


async def broadcast(payload: dict, exclude_user: Optional[str] = None) -> None:
    dead: list[str] = []
    data = json.dumps(payload, ensure_ascii=False)
    for uid, ws in list(ws_clients.items()):
        if exclude_user and uid == exclude_user:
            continue
        try:
            await ws.send_text(data)
        except Exception:
            dead.append(uid)
    for uid in dead:
        ws_clients.pop(uid, None)


async def push_fcm(user_id: str, title: str, body: str) -> None:
    conn = connect()
    row = conn.execute("SELECT token FROM fcm_tokens WHERE user_id = ?", (user_id,)).fetchone()
    conn.close()
    if not row:
        return
    # FCM requires firebase-admin credentials; log for now
    print(f"[FCM] → {user_id}: {title} — {body}", flush=True)


# ---------- health & auth ----------


@app.get("/health")
def health():
    return {
        "ok": True,
        "smsProvider": sms_provider(),
        "smsReady": sms_is_configured(),
        "wsClients": len(ws_clients),
        "publicUrl": PUBLIC_URL or None,
    }


@app.get("/admin/check")
def admin_check(authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    return {"isOwner": is_owner_user(user_id), "ownerUsername": OWNER_USERNAME}


@app.get("/admin/stats")
def admin_stats(authorization: Optional[str] = Header(None)):
    require_owner(authorization)
    conn = connect()
    users_total = conn.execute("SELECT COUNT(*) AS c FROM users").fetchone()["c"]
    messages_total = conn.execute("SELECT COUNT(*) AS c FROM messages").fetchone()["c"]
    chats_total = conn.execute("SELECT COUNT(*) AS c FROM chats").fetchone()["c"]
    accounts_total = conn.execute("SELECT COUNT(*) AS c FROM accounts").fetchone()["c"]
    online_db = conn.execute("SELECT COUNT(*) AS c FROM users WHERE is_online = 1").fetchone()["c"]
    day_start = int(datetime.now().replace(hour=0, minute=0, second=0, microsecond=0).timestamp() * 1000)
    messages_today = conn.execute(
        "SELECT COUNT(*) AS c FROM messages WHERE created_at >= ?", (day_start,)
    ).fetchone()["c"]
    last_msg = conn.execute(
        "SELECT created_at FROM messages ORDER BY created_at DESC LIMIT 1"
    ).fetchone()
    conn.close()
    return {
        "usersTotal": users_total,
        "messagesTotal": messages_total,
        "messagesToday": messages_today,
        "chatsTotal": chats_total,
        "accountsTotal": accounts_total,
        "onlineNow": max(online_db, len(ws_clients)),
        "wsConnections": len(ws_clients),
        "lastMessageAt": last_msg["created_at"] if last_msg else None,
        "publicUrl": PUBLIC_URL or None,
        "ownerUsername": OWNER_USERNAME,
    }


@app.post("/auth/register", response_model=AuthResponse)
def register(body: AuthRequest):
    for acc in accounts_db.values():
        if acc["phone"] == body.phone:
            raise HTTPException(400, "Phone already registered")
    account_id = str(uuid.uuid4())
    user_id = str(uuid.uuid4())
    token = str(uuid.uuid4())
    pwd_hash = hash_password(body.password)
    persist_account(account_id, body.phone, pwd_hash)
    user = {
        "id": user_id,
        "accountId": account_id,
        "displayName": body.displayName or "User",
        "username": None,
        "bio": "",
        "isOnline": True,
        "lastSeen": int(datetime.now().timestamp() * 1000),
        "isPremium": False,
    }
    persist_user(user)
    persist_token(token, user_id)
    return auth_response(user_id, account_id, token)


@app.post("/auth/login", response_model=AuthResponse)
def login(body: AuthRequest):
    for account_id, acc in accounts_db.items():
        if acc["phone"] == body.phone and acc["password_hash"] == hash_password(body.password):
            user_id = next((u["id"] for u in users_db.values() if u["accountId"] == account_id), None)
            if not user_id:
                raise HTTPException(404, "User not found")
            token = str(uuid.uuid4())
            persist_token(token, user_id)
            user = users_db[user_id]
            user["isOnline"] = True
            user["lastSeen"] = int(datetime.now().timestamp() * 1000)
            persist_user(user)
            return auth_response(user_id, account_id, token)
    raise HTTPException(401, "Invalid credentials")


@app.post("/auth/login/username", response_model=AuthResponse)
def login_username(body: UsernameLoginRequest):
    user = find_user_by_username(body.username)
    if not user:
        raise HTTPException(404, "Пользователь не найден")
    if not user.get("username"):
        raise HTTPException(400, "У аккаунта не задан @username")
    acc = accounts_db.get(user["accountId"])
    if not acc or acc["password_hash"] != hash_password(body.password):
        raise HTTPException(401, "Неверный пароль")
    token = str(uuid.uuid4())
    persist_token(token, user["id"])
    user["isOnline"] = True
    user["lastSeen"] = int(datetime.now().timestamp() * 1000)
    persist_user(user)
    return auth_response(user["id"], user["accountId"], token)


@app.post("/auth/register/web", response_model=AuthResponse)
def register_web(body: WebRegisterRequest):
    username = body.username.lstrip("@").strip().lower()
    if len(username) < 3:
        raise HTTPException(400, "Username минимум 3 символа")
    if len(body.password) < 8:
        raise HTTPException(400, "Пароль минимум 8 символов")
    if find_user_by_username(username):
        raise HTTPException(400, "Username занят")
    phone = f"web:{username}"
    for acc in accounts_db.values():
        if acc["phone"] == phone:
            raise HTTPException(400, "Аккаунт уже существует")
    account_id = str(uuid.uuid4())
    user_id = str(uuid.uuid4())
    token = str(uuid.uuid4())
    pwd_hash = hash_password(body.password)
    persist_account(account_id, phone, pwd_hash)
    user = {
        "id": user_id,
        "accountId": account_id,
        "displayName": body.displayName.strip() or username,
        "username": username,
        "bio": "",
        "isOnline": True,
        "lastSeen": int(datetime.now().timestamp() * 1000),
        "isPremium": False,
    }
    persist_user(user)
    persist_token(token, user_id)
    return auth_response(user_id, account_id, token)


@app.post("/auth/qr", response_model=AuthResponse)
def qr_login(body: QrAuthRequest):
    if not tokens_db:
        raise HTTPException(400, "No active sessions")
    user_id = next(iter(tokens_db.values()))
    user = users_db[user_id]
    token = str(uuid.uuid4())
    persist_token(token, user_id)
    return auth_response(user_id, user["accountId"], token)


@app.post("/auth/sms/send", response_model=SmsSendResponse)
def sms_send(body: SmsSendRequest):
    try:
        phone = normalize_phone(body.phone)
    except ValueError:
        raise HTTPException(400, "Invalid phone number")
    now = time.time()
    existing = sms_codes_db.get(phone)
    if existing and now - existing["sent_at"] < SMS_RESEND_COOLDOWN_SEC:
        wait = int(SMS_RESEND_COOLDOWN_SEC - (now - existing["sent_at"]))
        raise HTTPException(429, f"Retry after {wait} seconds")
    code = generate_code()
    result = send_sms(f"+{phone}", f"{code} — код для входа в Teleport.\nНе сообщайте никому.")
    if not result.ok:
        raise HTTPException(503, result.error or "SMS failed")
    sms_codes_db[phone] = {"code": code, "expires": now + SMS_CODE_TTL_SEC, "sent_at": now}
    return SmsSendResponse(ok=True, devCode=code if result.provider == "mock" else None, retryAfter=SMS_RESEND_COOLDOWN_SEC)


@app.post("/auth/sms/verify", response_model=SmsVerifyResponse)
def sms_verify(body: SmsVerifyRequest):
    try:
        phone = normalize_phone(body.phone)
    except ValueError:
        raise HTTPException(400, "Invalid phone number")
    entry = sms_codes_db.get(phone)
    if not entry or time.time() > entry["expires"]:
        raise HTTPException(400, "Code expired")
    if entry["code"] != body.code.strip():
        raise HTTPException(400, "Invalid code")
    sms_codes_db.pop(phone, None)
    return SmsVerifyResponse(ok=True)


@app.post("/auth/recover")
def recover(body: AuthRequest):
    for account_id, acc in accounts_db.items():
        if acc["phone"] == body.phone:
            acc["password_hash"] = hash_password(body.password)
            persist_account(account_id, acc["phone"], acc["password_hash"])
            return {"ok": True}
    raise HTTPException(404, "Account not found")


@app.get("/users/me")
def get_me(authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    user = users_db.get(user_id)
    if not user:
        raise HTTPException(404, "User not found")
    return {
        "id": user["id"],
        "displayName": user["displayName"],
        "username": user.get("username"),
        "bio": user.get("bio", ""),
        "isOnline": user.get("isOnline", False),
        "lastSeen": user.get("lastSeen", 0),
        "isPremium": user.get("isPremium", False),
    }


@app.patch("/users/me")
def update_me(body: UpdateProfileRequest, authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    user = users_db.get(user_id)
    if not user:
        raise HTTPException(404, "User not found")
    if body.username:
        taken = any(
            u.get("username", "").lower() == body.username.lower() and u["id"] != user_id
            for u in users_db.values()
        )
        if taken:
            raise HTTPException(400, "Username taken")
        user["username"] = body.username.lstrip("@")
    if body.displayName is not None:
        user["displayName"] = body.displayName
    if body.bio is not None:
        user["bio"] = body.bio
    persist_user(user)
    return get_me(authorization)


@app.get("/users/{user_id}")
def get_user(user_id: str, authorization: Optional[str] = Header(None)):
    auth_user_id(authorization)
    user = users_db.get(user_id)
    if not user:
        raise HTTPException(404, "User not found")
    return {
        "id": user["id"],
        "displayName": user["displayName"],
        "username": user.get("username"),
        "bio": user.get("bio", ""),
        "isOnline": user.get("isOnline", False),
        "lastSeen": user.get("lastSeen", 0),
        "isPremium": user.get("isPremium", False),
    }


@app.get("/users/search")
def search_users(q: str, authorization: Optional[str] = Header(None)):
    me = auth_user_id(authorization)
    q = q.lower().strip()
    if len(q) < 1:
        return []
    return [
        {
            "id": u["id"],
            "displayName": u["displayName"],
            "username": u.get("username"),
            "bio": u.get("bio", ""),
            "isOnline": u.get("isOnline", False),
            "lastSeen": u.get("lastSeen", 0),
            "isPremium": u.get("isPremium", False),
        }
        for u in users_db.values()
        if u["id"] != me
        and (
            q in u["displayName"].lower()
            or (u.get("username") and q in u["username"].lower())
        )
    ]


@app.get("/users/username/{username}/available")
def check_username(username: str, exclude: Optional[str] = None):
    taken = any(
        u.get("username", "").lower() == username.lower() and u["id"] != exclude for u in users_db.values()
    )
    return {"available": not taken}


# ---------- chats ----------


@app.post("/chats/open")
def open_chat(body: OpenChatRequest, authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    if body.otherUserId == user_id:
        raise HTTPException(400, "Cannot chat with yourself")
    chat = ensure_private_chat(user_id, body.otherUserId)
    other = users_db[body.otherUserId]
    return {
        **chat,
        "peer": {
            "id": other["id"],
            "displayName": other["displayName"],
            "username": other.get("username"),
            "bio": other.get("bio", ""),
            "isOnline": other.get("isOnline", False),
            "lastSeen": other.get("lastSeen", 0),
            "isPremium": other.get("isPremium", False),
        },
    }


@app.get("/chats")
def list_chats(authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    conn = connect()
    rows = conn.execute(
        """
        SELECT c.id, c.type, c.title
        FROM chats c
        INNER JOIN chat_members cm ON cm.chat_id = c.id
        WHERE cm.user_id = ?
        ORDER BY c.created_at DESC
        """,
        (user_id,),
    ).fetchall()
    conn.close()
    result = []
    for row in rows:
        members = get_chat_members(row["id"])
        peer_id = next((m for m in members if m != user_id), None)
        title = row["title"]
        if peer_id and peer_id in users_db:
            title = users_db[peer_id]["displayName"]
        result.append({"chatId": row["id"], "type": row["type"], "title": title, "members": members})
    return result


@app.get("/chats/{chat_id}/messages")
def get_chat_messages(
    chat_id: str,
    since: int = 0,
    limit: int = 200,
    authorization: Optional[str] = Header(None),
):
    user_id = auth_user_id(authorization)
    if not user_is_chat_member(user_id, chat_id):
        raise HTTPException(403, "Not a chat member")
    conn = connect()
    rows = conn.execute(
        "SELECT * FROM messages WHERE chat_id = ? AND created_at > ? ORDER BY created_at ASC LIMIT ?",
        (chat_id, since, min(limit, 500)),
    ).fetchall()
    conn.close()
    return {"messages": [row_to_msg(r) for r in rows]}


# ---------- messages & media ----------


@app.post("/devices/fcm")
def register_fcm(body: FcmTokenRequest, authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    conn = connect()
    conn.execute(
        "INSERT OR REPLACE INTO fcm_tokens (user_id, token) VALUES (?, ?)",
        (user_id, body.token),
    )
    conn.commit()
    conn.close()
    return {"ok": True}


@app.post("/media/upload")
async def upload_media(authorization: Optional[str] = Header(None), file: UploadFile = File(...)):
    auth_user_id(authorization)
    ext = Path(file.filename or "bin").suffix or ".bin"
    name = f"{uuid.uuid4()}{ext}"
    dest = UPLOAD_DIR / name
    with dest.open("wb") as out:
        shutil.copyfileobj(file.file, out)
    return {"url": f"/media/{name}"}


@app.get("/media/{filename}")
def get_media(filename: str):
    path = UPLOAD_DIR / filename
    if not path.exists():
        raise HTTPException(404)
    return FileResponse(path)


@app.post("/messages/send")
async def send_message(body: SendMessageRequest, authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    if not user_is_chat_member(user_id, body.chatId):
        raise HTTPException(403, "Not a chat member")
    msg = {
        "id": body.id or str(uuid.uuid4()),
        "chatId": body.chatId,
        "senderId": user_id,
        "type": body.type,
        "text": body.text,
        "mediaUri": body.mediaUri,
        "replyToId": body.replyToId,
        "forwardFromId": body.forwardFromId,
        "isEdited": False,
        "editedAt": None,
        "createdAt": int(datetime.now().timestamp() * 1000),
    }
    save_message(msg)
    await notify_chat(body.chatId, {"event": "message", "payload": msg}, exclude_user=user_id)
    return msg


@app.get("/messages/sync")
def sync_messages(since: int = 0, authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    chat_ids = get_user_chat_ids(user_id)
    if not chat_ids:
        return {"messages": []}
    conn = connect()
    placeholders = ",".join("?" * len(chat_ids))
    rows = conn.execute(
        f"SELECT * FROM messages WHERE created_at > ? AND chat_id IN ({placeholders}) ORDER BY created_at ASC",
        (since, *chat_ids),
    ).fetchall()
    conn.close()
    return {"messages": [row_to_msg(r) for r in rows]}


@app.patch("/messages/{message_id}")
async def edit_message(message_id: str, body: EditMessageRequest, authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    conn = connect()
    row = conn.execute("SELECT * FROM messages WHERE id = ?", (message_id,)).fetchone()
    if not row:
        conn.close()
        raise HTTPException(404)
    if row["sender_id"] != user_id:
        conn.close()
        raise HTTPException(403)
    edited_at = int(datetime.now().timestamp() * 1000)
    conn.execute(
        "UPDATE messages SET text = ?, is_edited = 1, edited_at = ? WHERE id = ?",
        (body.text, edited_at, message_id),
    )
    conn.commit()
    row = conn.execute("SELECT * FROM messages WHERE id = ?", (message_id,)).fetchone()
    conn.close()
    msg = row_to_msg(row)
    await notify_chat(msg["chatId"], {"event": "message_updated", "payload": msg}, exclude_user=user_id)
    return msg


@app.post("/messages/{message_id}/forward")
async def forward_message(message_id: str, body: ForwardMessageRequest, authorization: Optional[str] = Header(None)):
    user_id = auth_user_id(authorization)
    conn = connect()
    original = conn.execute("SELECT * FROM messages WHERE id = ?", (message_id,)).fetchone()
    conn.close()
    if not original:
        raise HTTPException(404)
    msg = {
        "id": str(uuid.uuid4()),
        "chatId": body.toChatId,
        "senderId": user_id,
        "type": original["type"],
        "text": original["text"] or "",
        "mediaUri": original["media_uri"],
        "replyToId": None,
        "forwardFromId": message_id,
        "isEdited": False,
        "editedAt": None,
        "createdAt": int(datetime.now().timestamp() * 1000),
    }
    save_message(msg)
    await notify_chat(body.toChatId, {"event": "message", "payload": msg}, exclude_user=user_id)
    return msg


# ---------- WebSocket realtime + call signaling ----------


@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket, token: str):
    user_id = tokens_db.get(token)
    if not user_id:
        await websocket.close(code=4401)
        return
    await websocket.accept()
    ws_clients[user_id] = websocket
    if user_id in users_db:
        users_db[user_id]["isOnline"] = True
        persist_user(users_db[user_id])
    try:
        while True:
            raw = await websocket.receive_text()
            data = json.loads(raw)
            event = data.get("event")
            payload = data.get("payload", {})
            payload["fromUserId"] = user_id
            if event == "call_signal":
                chat_id = payload.get("chatId")
                target = payload.get("targetUserId")
                if not target and chat_id:
                    members = get_chat_members(chat_id)
                    target = next((m for m in members if m != user_id), None)
                    payload["targetUserId"] = target
                if target and target in ws_clients:
                    await ws_clients[target].send_text(
                        json.dumps({"event": "call_signal", "payload": payload}, ensure_ascii=False)
                    )
            else:
                await broadcast({"event": event, "payload": payload}, exclude_user=user_id)
    except WebSocketDisconnect:
        pass
    finally:
        if ws_clients.get(user_id) is websocket:
            ws_clients.pop(user_id, None)
        if user_id in users_db:
            users_db[user_id]["isOnline"] = False
            users_db[user_id]["lastSeen"] = int(datetime.now().timestamp() * 1000)
            persist_user(users_db[user_id])


# ---------- Web UI ----------


@app.get("/")
def web_index():
    index = WEB_DIR / "index.html"
    if not index.exists():
        return {"ok": True, "api": "Teleport", "web": "missing — add teleport/web/"}
    return FileResponse(index)


@app.get("/showcase")
def web_showcase():
    page = WEB_DIR / "showcase.html"
    if not page.exists():
        return {"ok": False, "detail": "showcase.html not found"}
    return FileResponse(page)


if WEB_ASSETS.is_dir():
    app.mount("/assets", StaticFiles(directory=WEB_ASSETS), name="web-assets")


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host=HOST, port=PORT)
