#!/usr/bin/env bash
set -euo pipefail

BUILD_FLAG=""
if [[ "${1:-}" == "--build" ]]; then
  BUILD_FLAG="--build"
fi

use_default_if_missing_or_placeholder() {
  local name="$1"
  local default_value="$2"
  local current_value="${!name:-}"

  if [[ -z "$current_value" || "$current_value" == \<YOUR_* ]]; then
    export "$name=$default_value"
    echo "Usando valor local padrao para $name."
  fi
}

is_port_in_use() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1
    return
  fi
  if command -v ss >/dev/null 2>&1; then
    ss -ltn "( sport = :$port )" | grep -q "$port"
    return
  fi
  return 1
}

resolve_host_port() {
  local name="$1"
  shift
  local candidates=("$@")
  local preferred="${candidates[0]}"

  for candidate in "${candidates[@]}"; do
    if ! is_port_in_use "$candidate"; then
      if [[ "$candidate" != "$preferred" ]]; then
        echo "Porta $preferred ocupada para $name. Usando fallback $candidate." >&2
      fi
      echo "$candidate"
      return
    fi
  done

  echo "Nenhuma porta livre encontrada para $name. Candidatas testadas: ${candidates[*]}" >&2
  return 1
}

export DB_HOST_PORT="$(resolve_host_port PostgreSQL 5432 5433 15432 25432 35432)"
export REDIS_HOST_PORT="$(resolve_host_port Redis 6379 6380 16379 26379)"
export BACKEND_HOST_PORT="$(resolve_host_port Backend 8080 8081 18080 28080)"
export FRONTEND_HOST_PORT="$(resolve_host_port Frontend 80 8088 3000 4173)"

use_default_if_missing_or_placeholder DB_NAME usuarios_db
use_default_if_missing_or_placeholder DB_USER postgres
use_default_if_missing_or_placeholder DB_PASS postgres
use_default_if_missing_or_placeholder JWT_SECRET ZGV2LWp3dC1zZWNyZXQtZm9yLWxvY2FsLXRlc3Rpbmctb25seS1yZXBsYWNlLWluLXByb2Q=

echo "Subindo stack local com portas:"
echo "  PostgreSQL -> $DB_HOST_PORT"
echo "  Redis      -> $REDIS_HOST_PORT"
echo "  Backend    -> $BACKEND_HOST_PORT"
echo "  Frontend   -> $FRONTEND_HOST_PORT"

docker compose up -d ${BUILD_FLAG}

echo
echo "Stack iniciada."
echo "Frontend: http://localhost:$FRONTEND_HOST_PORT"
echo "Backend:  http://localhost:$BACKEND_HOST_PORT"
