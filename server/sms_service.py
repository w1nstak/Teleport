"""SMS delivery for Teleport phone auth."""
from __future__ import annotations

import base64
import json
import logging
import os
import random
import urllib.parse
import urllib.request
from dataclasses import dataclass
from typing import Literal

logger = logging.getLogger("teleport.sms")

Provider = Literal["mock", "smsru", "smsc", "twilio"]


@dataclass
class SendResult:
    ok: bool
    provider: Provider
    error: str | None = None


def sms_provider() -> str:
    return os.getenv("SMS_PROVIDER", "mock").strip().lower()


def sms_is_configured() -> bool:
    provider = sms_provider()
    if provider == "mock":
        return True
    if provider == "smsru":
        return bool(os.getenv("SMSRU_API_ID", "").strip())
    if provider == "smsc":
        login = os.getenv("SMSC_LOGIN", "").strip()
        password = os.getenv("SMSC_PASSWORD", "").strip()
        return bool(login and password)
    if provider == "twilio":
        return all(
            os.getenv(k, "").strip()
            for k in ("TWILIO_ACCOUNT_SID", "TWILIO_AUTH_TOKEN", "TWILIO_FROM_NUMBER")
        )
    return False


def normalize_phone(phone: str) -> str:
    digits = "".join(ch for ch in phone if ch.isdigit())
    if not digits:
        raise ValueError("Invalid phone number")
    if digits.startswith("8") and len(digits) == 11:
        digits = "7" + digits[1:]
    return digits


def send_sms(phone: str, message: str) -> SendResult:
    provider = sms_provider()
    digits = normalize_phone(phone)

    if provider == "mock":
        logger.info("[mock] +%s: %s", digits, message)
        print(f"[SMS mock] to +{digits}: {message}", flush=True)
        return SendResult(ok=True, provider="mock")

    if provider == "smsru":
        return _send_smsru(digits, message)

    if provider == "smsc":
        return _send_smsc(digits, message)

    if provider == "twilio":
        return _send_twilio(digits, message)

    return SendResult(ok=False, provider="mock", error=f"Неизвестный SMS_PROVIDER: {provider}")


def _http_get_json(url: str) -> dict:
    with urllib.request.urlopen(url, timeout=25) as resp:
        return json.loads(resp.read().decode())


def _send_smsru(phone_digits: str, message: str) -> SendResult:
    api_id = os.getenv("SMSRU_API_ID", "").strip()
    if not api_id:
        return SendResult(ok=False, provider="smsru", error="Не задан SMSRU_API_ID в server/.env")

    params: dict[str, str] = {
        "api_id": api_id,
        "to": phone_digits,
        "msg": message,
        "json": "1",
    }
    sender = os.getenv("SMSRU_FROM", "").strip()
    if sender:
        params["from"] = sender

    url = f"https://sms.ru/sms/send?{urllib.parse.urlencode(params)}"
    try:
        data = _http_get_json(url)
    except Exception as exc:
        logger.exception("sms.ru request failed")
        return SendResult(ok=False, provider="smsru", error=f"sms.ru: {exc}")

    if data.get("status") != "OK":
        return SendResult(
            ok=False,
            provider="smsru",
            error=data.get("status_text") or str(data),
        )

    sms_map = data.get("sms") or {}
    entry = sms_map.get(phone_digits) or next(iter(sms_map.values()), None)
    if entry and entry.get("status") == "OK":
        logger.info("sms.ru sent to +%s id=%s", phone_digits, entry.get("sms_id"))
        return SendResult(ok=True, provider="smsru")

    err = (entry or {}).get("status_text") or "sms.ru: ошибка доставки"
    return SendResult(ok=False, provider="smsru", error=err)


def _send_smsc(phone_digits: str, message: str) -> SendResult:
    login = os.getenv("SMSC_LOGIN", "").strip()
    password = os.getenv("SMSC_PASSWORD", "").strip()
    if not login or not password:
        return SendResult(ok=False, provider="smsc", error="Не заданы SMSC_LOGIN / SMSC_PASSWORD в server/.env")

    params = urllib.parse.urlencode(
        {
            "login": login,
            "psw": password,
            "phones": phone_digits,
            "mes": message,
            "fmt": "3",
            "charset": "utf-8",
        }
    )
    url = f"https://smsc.ru/sys/send.php?{params}"
    try:
        data = _http_get_json(url)
    except Exception as exc:
        logger.exception("smsc.ru request failed")
        return SendResult(ok=False, provider="smsc", error=f"smsc.ru: {exc}")

    if "error" in data:
        return SendResult(ok=False, provider="smsc", error=str(data["error"]))

    logger.info("smsc.ru sent to +%s id=%s", phone_digits, data.get("id"))
    return SendResult(ok=True, provider="smsc")


def _send_twilio(phone_digits: str, message: str) -> SendResult:
    account_sid = os.getenv("TWILIO_ACCOUNT_SID", "").strip()
    auth_token = os.getenv("TWILIO_AUTH_TOKEN", "").strip()
    from_number = os.getenv("TWILIO_FROM_NUMBER", "").strip()
    if not all([account_sid, auth_token, from_number]):
        return SendResult(ok=False, provider="twilio", error="Twilio env vars are not set")

    to_number = f"+{phone_digits}"
    payload = urllib.parse.urlencode({"To": to_number, "From": from_number, "Body": message}).encode()
    url = f"https://api.twilio.com/2010-04-01/Accounts/{account_sid}/Messages.json"
    req = urllib.request.Request(url, data=payload, method="POST")
    credentials = f"{account_sid}:{auth_token}".encode()
    req.add_header("Authorization", f"Basic {base64.b64encode(credentials).decode()}")
    req.add_header("Content-Type", "application/x-www-form-urlencoded")
    try:
        with urllib.request.urlopen(req, timeout=25) as resp:
            if 200 <= resp.status < 300:
                return SendResult(ok=True, provider="twilio")
            body = resp.read().decode()
            return SendResult(ok=False, provider="twilio", error=body)
    except Exception as exc:
        return SendResult(ok=False, provider="twilio", error=str(exc))


def generate_code() -> str:
    return f"{random.randint(100000, 999999)}"
