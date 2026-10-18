@echo off
REM Start local MySQL database container
docker compose -f docker-compose.db.yml up -d
