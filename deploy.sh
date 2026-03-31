#!/usr/bin/env bash
set -euo pipefail

# -------------------------------------------------------
# Production deploy script
# Usage: ./deploy.sh [--pull]
# -------------------------------------------------------

COMPOSE_FILE="docker-compose.prod.yml"
PROJECT_NAME="usuarios"

echo "==> Checking .env file..."
if [ ! -f ".env" ]; then
  echo "ERROR: .env file not found. Copy .env.example and fill in the values."
  exit 1
fi

# Optional: pull latest code
if [[ "${1:-}" == "--pull" ]]; then
  echo "==> Pulling latest code..."
  git pull origin main
fi

echo "==> Building images..."
docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" build --no-cache

echo "==> Stopping old containers..."
docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" down --remove-orphans

echo "==> Starting services..."
docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d

echo "==> Waiting for health checks..."
sleep 15

echo "==> Service status:"
docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" ps

echo ""
echo "Deploy complete! Application running at http://localhost"
echo "Backend health: http://localhost/api/actuator/health"
