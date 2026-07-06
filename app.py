"""Точка входа для Amvera Cloud (python3 app.py)."""
from __future__ import annotations

import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SERVER_DIR = ROOT / "server"

sys.path.insert(0, str(SERVER_DIR))
os.environ.setdefault("WEB_DIR", str(ROOT / "web"))
if os.environ.get("AMVERA") or os.environ.get("DATA_DIR") == "/data":
    os.environ.setdefault("DATA_DIR", "/data")

import uvicorn  # noqa: E402
from main import app  # noqa: E402

if __name__ == "__main__":
    port = int(os.environ.get("PORT", "5000"))
    uvicorn.run(app, host="0.0.0.0", port=port)
