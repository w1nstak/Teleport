FROM python:3.10-slim

WORKDIR /app

COPY requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY app.py ./app.py
COPY server/ ./server/
COPY web/ ./web/

ENV AMVERA=1
ENV DATA_DIR=/data
ENV WEB_DIR=/app/web
ENV PORT=5000
ENV HOST=0.0.0.0
ENV PUBLIC_URL=

VOLUME /data
EXPOSE 5000

CMD ["python", "app.py"]
