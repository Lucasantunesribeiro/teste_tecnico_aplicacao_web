#!/usr/bin/env bash
set -euo pipefail

# -------------------------------------------------------
# Production deploy script
# Usage: ./deploy.sh [--pull]
# -------------------------------------------------------

COMPOSE_FILE="docker-compose.prod.yml"
PROJECT_NAME="usuarios"

# -------------------------------------------------------
# Verificação de prerequisites
# -------------------------------------------------------
echo "==> Checking prerequisites..."

if ! command -v docker &> /dev/null; then
  echo "ERROR: Docker não encontrado. Instale Docker e tente novamente."
  echo "       https://docs.docker.com/get-docker/"
  exit 1
fi

if ! docker compose version &> /dev/null; then
  echo "ERROR: Docker Compose não encontrado. Instale o plugin Compose:"
  echo "       https://docs.docker.com/compose/install/"
  exit 1
fi

# -------------------------------------------------------
# Verificação do .env e variáveis obrigatórias
# -------------------------------------------------------
echo "==> Checking .env file..."

if [ ! -f ".env" ]; then
  echo "ERROR: .env file not found. Copy .env.example and fill in the values."
  echo "       cp .env.example .env"
  exit 1
fi

# Carrega variáveis do .env para checar presença (sem exportar para o shell)
set -a
# shellcheck disable=SC1091
source .env
set +a

required_vars=("JWT_SECRET" "DB_PASS" "DB_HOST" "DB_USER" "DB_NAME")
missing=()

for var in "${required_vars[@]}"; do
  if [ -z "${!var:-}" ]; then
    missing+=("$var")
  fi
done

if [ ${#missing[@]} -gt 0 ]; then
  echo "ERROR: As seguintes variáveis obrigatórias não estão definidas em .env:"
  for var in "${missing[@]}"; do
    echo "       - $var"
  done
  exit 1
fi

# Valida que JWT_SECRET tem comprimento mínimo (segurança)
if [ ${#JWT_SECRET} -lt 32 ]; then
  echo "ERROR: JWT_SECRET deve ter no mínimo 32 caracteres."
  echo "       Gere uma chave segura com: openssl rand -base64 64"
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
