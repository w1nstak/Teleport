FROM python:3.12-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

COPY server/requirements.txt ./server/requirements.txt
RUN pip install --no-cache-dir -r server/requirements.txt

COPY server/ ./server/
COPY web/ ./web/

ENV DATA_DIR=/data
ENV WEB_DIR=/app/web
ENV PORT=8765
ENV HOST=0.0.0.0

VOLUME /data
EXPOSE 8765

WORKDIR /app/server
CMD ["sh", "-c", "uvicorn main:app --host ${HOST} --port ${PORT}"]
