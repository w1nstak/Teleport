"""Paths and runtime config for Teleport server."""
from __future__ import annotations

import os
from pathlib import Path

try:
    from dotenv import load_dotenv

    load_dotenv(Path(__file__).parent / ".env")
except ImportError:
    pass

_ROOT = Path(__file__).parent


def _resolve_data_dir() -> Path:
    if os.environ.get("DATA_DIR"):
        return Path(os.environ["DATA_DIR"])
    if os.environ.get("AMVERA"):
        return Path("/data")
    return _ROOT


DATA_DIR = _resolve_data_dir()
DB_PATH = DATA_DIR / "teleport.db"
UPLOAD_DIR = DATA_DIR / "uploads"
WEB_DIR = Path(os.environ.get("WEB_DIR", _ROOT.parent / "web"))

PORT = int(os.environ.get("PORT", "8765"))
HOST = os.environ.get("HOST", "0.0.0.0")

# Public URL without trailing slash, e.g. https://api.example.com
PUBLIC_URL = os.environ.get("PUBLIC_URL", "").rstrip("/")

# Владелец / админ (username без @, без учёта регистра)
OWNER_USERNAME = os.environ.get("OWNER_USERNAME", "w1nst").strip().lstrip("@").lower()
