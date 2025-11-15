#!/usr/bin/env bash
set -euo pipefail

# Default container images
PLAYWRIGHT_IMAGE="${PLAYWRIGHT_IMAGE:-goldmanager/e2e-playwright:local}"
PLAYWRIGHT_VERSION="${PLAYWRIGHT_VERSION:-1.54.2}"
APP_IMAGE_DEFAULT="goldmanager:latest"
APP_IMAGE_EXPLICIT=0
if [[ -n "${APP_IMAGE:-}" ]]; then
  APP_IMAGE_VALUE="${APP_IMAGE}"
  APP_IMAGE_EXPLICIT=1
else
  APP_IMAGE_VALUE="${APP_IMAGE_DEFAULT}"
fi
APP_IMAGE="${APP_IMAGE_VALUE}"

# Backend runtime defaults
APP_PORT="${E2E_APP_PORT:-8080}"
APP_CONTAINER="${E2E_APP_CONTAINER:-goldmanager-e2e-app}"
DB_HOST_CONTAINER="${E2E_DB_HOST:-host.docker.internal}"
if [[ "${DB_HOST_CONTAINER}" == "host.docker.internal" ]]; then
  DB_HOST_LOCAL="127.0.0.1"
else
  DB_HOST_LOCAL="${DB_HOST_CONTAINER}"
fi
DB_PORT="${E2E_DB_PORT:-3317}"
DB_USER="${E2E_DB_USERNAME:-myuser}"
DB_PASS="${E2E_DB_PASSWORD:-mypass}"
APP_DEFAULT_USER="${APP_DEFAULT_USER:-admin}"
APP_DEFAULT_PASSWORD="${APP_DEFAULT_PASSWORD:-admin1Password!}"
SPRING_PROFILES="${SPRING_PROFILES_ACTIVE:-dev}"

# E2E waits
declare -x E2E_DB_WAIT_MS="${E2E_DB_WAIT_MS:-360000}"
declare -x E2E_HEALTH_TIMEOUT_MS="${E2E_HEALTH_TIMEOUT_MS:-600000}"
declare -x E2E_HEALTH_POLL_MS="${E2E_HEALTH_POLL_MS:-1000}"
declare -x E2E_WEBSERVER_TIMEOUT_MS="${E2E_WEBSERVER_TIMEOUT_MS:-720000}"

COMPOSE_FILE="e2e/dev-db/compose.yaml"
RESET_MODE="${E2E_DB_RESET_MODE:-drop}"  # 'drop' or 'compose'
EXTRA_HOST="--add-host=host.docker.internal:host-gateway"

CLEAN_DB=0
FIX_PERMS=0
FIX_PERMS_BACKEND=0
FIX_PERMS_REPORTS=0
VERBOSE=0
SHOW_HELP=0
PASS_ARGS=()

usage() {
  cat <<'USAGE'
Playwright E2E runner (single script for agents and humans)

Usage:
  bash ./e2e/run-in-docker.sh [options] [--] [playwright-args]

Options:
  --app-image <image[:tag]>  Backend Docker image to test against (default: goldmanager:latest).
                             When omitted, this script builds the root Dockerfile before running tests.
  --clean-db                 Reset the dedicated E2E MariaDB via docker compose down -v; up -d.
  --fix-perms                Fix ownership for backend/build and e2e/test-results* via root container.
  --fix-perms-backend        Fix ownership for backend/build only.
  --fix-perms-reports        Fix ownership for e2e/test-results* only.
  --verbose                  Enable verbose logging in the Playwright container.
  -h, --help                 Show this help message.

Notes:
  - Requires Docker plus Node/Java toolchains on the host for occasional steps (npm ci, Gradle, etc.).
  - Builds the Playwright image (goldmanager/e2e-playwright:local) if missing.
  - Starts/ensures the E2E MariaDB (e2e/dev-db/compose.yaml) and drops the goldmanager DB by default.
  - Launches the backend as a Docker container and points Playwright to http://host.docker.internal:8080.
  - Pass Playwright CLI args after --, e.g.: -- --project=chromium tests/login.spec.ts

Examples:
  bash ./e2e/run-in-docker.sh
  bash ./e2e/run-in-docker.sh --app-image myregistry/goldmanager:1.2.3
  bash ./e2e/run-in-docker.sh --clean-db -- --project=chromium
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --app-image)
      [[ $# -ge 2 ]] || { echo "[e2e] Missing value for --app-image" >&2; exit 1; }
      APP_IMAGE="$2"
      APP_IMAGE_EXPLICIT=1
      shift 2
      ;;
    --app-image=*)
      APP_IMAGE="${1#*=}"
      APP_IMAGE_EXPLICIT=1
      shift 1
      ;;
    --clean-db)
      CLEAN_DB=1
      shift 1
      ;;
    --fix-perms)
      FIX_PERMS=1
      shift 1
      ;;
    --fix-perms-backend)
      FIX_PERMS_BACKEND=1
      shift 1
      ;;
    --fix-perms-reports)
      FIX_PERMS_REPORTS=1
      shift 1
      ;;
    --verbose)
      VERBOSE=1
      shift 1
      ;;
    -h|--help)
      SHOW_HELP=1
      shift 1
      ;;
    --)
      shift 1
      while [[ $# -gt 0 ]]; do
        PASS_ARGS+=("$1")
        shift 1
      done
      break
      ;;
    *)
      PASS_ARGS+=("$1")
      shift 1
      ;;
  esac
done

if [[ ${SHOW_HELP} -eq 1 ]]; then
  usage
  exit 0
fi

E2E_TEST_ARGS="${PASS_ARGS[*]:-}"

log() {
  echo "[e2e] $*"
}

APP_CONTAINER_STARTED=0
cleanup() {
  if [[ ${APP_CONTAINER_STARTED} -eq 1 ]]; then
    log "Stopping backend container ${APP_CONTAINER} ..."
    docker rm -f "${APP_CONTAINER}" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

# Ensure Playwright image exists
if ! docker image inspect "${PLAYWRIGHT_IMAGE}" >/dev/null 2>&1; then
  log "Building Playwright image ${PLAYWRIGHT_IMAGE} ..."
  docker build --build-arg PLAYWRIGHT_VERSION="${PLAYWRIGHT_VERSION}" -f e2e/Dockerfile -t "${PLAYWRIGHT_IMAGE}" .
fi

# Optionally fix permissions
if [[ ${FIX_PERMS} -eq 1 || ${FIX_PERMS_BACKEND} -eq 1 || ${FIX_PERMS_REPORTS} -eq 1 ]]; then
  log "Fixing permissions via root container ..."
  TARGETS=()
  if [[ ${FIX_PERMS} -eq 1 || ${FIX_PERMS_BACKEND} -eq 1 ]]; then
    TARGETS+=("/work/backend/build")
  fi
  if [[ ${FIX_PERMS} -eq 1 || ${FIX_PERMS_REPORTS} -eq 1 ]]; then
    TARGETS+=("/work/e2e/test-results" "/work/e2e/test-results-html")
  fi
  if [[ ${#TARGETS[@]} -gt 0 ]]; then
    docker run --rm -v "${PWD}":/work alpine:3.19 sh -lc "\
      mkdir -p /work/backend/build /work/e2e/test-results /work/e2e/test-results-html && \
      chown -R \$(id -u):\$(id -g) ${TARGETS[*]}" || true
  fi
fi

# Ensure/clean DB
if [[ ${CLEAN_DB} -eq 1 ]]; then
  log "CLEAN_DB requested -> docker compose down -v"
  docker compose -f "${COMPOSE_FILE}" down -v || true
fi
log "Ensuring E2E DB is running using ${COMPOSE_FILE} ..."
docker compose -f "${COMPOSE_FILE}" up -d

wait_for_db() {
  local deadline=$((SECONDS + 600))
  while true; do
    local health
    health=$(docker inspect -f '{{.State.Health.Status}}' goldmanager-e2e-mariadb 2>/dev/null || echo unknown)
    local tcp_ok=0
    if command -v nc >/dev/null 2>&1; then
      if nc -z "${DB_HOST_LOCAL}" "${DB_PORT}" >/dev/null 2>&1; then
        tcp_ok=1
      fi
    else
      if (echo > /dev/tcp/${DB_HOST_LOCAL}/${DB_PORT}) >/dev/null 2>&1; then
        tcp_ok=1
      fi
    fi
    if [[ "${health}" == "healthy" || ${tcp_ok} -eq 1 ]]; then
      log "DB ready: health=${health} tcp=${tcp_ok}"
      return 0
    fi
    if (( SECONDS > deadline )); then
      log "Timeout waiting for DB health"
      return 1
    fi
    log "Waiting for DB health (health=${health}, tcp=${tcp_ok}) ..."
    sleep 5
  done
}

wait_for_db || {
  log "Database did not become healthy"
  docker logs --tail 200 goldmanager-e2e-mariadb || true
  exit 1
}

if [[ "${RESET_MODE}" == "drop" ]]; then
  log "Dropping and recreating database 'goldmanager' for clean state ..."
  if ! docker exec goldmanager-e2e-mariadb sh -lc "mariadb -h 127.0.0.1 -uroot -p\$MARIADB_ROOT_PASSWORD -e \"DROP DATABASE IF EXISTS goldmanager; CREATE DATABASE goldmanager; GRANT ALL PRIVILEGES ON goldmanager.* TO 'myuser'@'%'; FLUSH PRIVILEGES;\""; then
    log "Database reset failed"
    docker logs --tail 200 goldmanager-e2e-mariadb || true
    exit 1
  fi
fi

# Build or validate backend image
if [[ ${APP_IMAGE_EXPLICIT} -eq 0 ]]; then
  log "Building backend image ${APP_IMAGE} using root Dockerfile ..."
  DOCKER_BUILDKIT=${DOCKER_BUILDKIT:-1} docker build -t "${APP_IMAGE}" .
else
  if ! docker image inspect "${APP_IMAGE}" >/dev/null 2>&1; then
    log "Backend image ${APP_IMAGE} not found. Please build or pull it first."
    exit 1
  fi
fi

start_backend() {
  log "Starting backend container ${APP_CONTAINER} from ${APP_IMAGE} ..."
  docker rm -f "${APP_CONTAINER}" >/dev/null 2>&1 || true
  docker run -d --name "${APP_CONTAINER}" ${EXTRA_HOST} \
    -p "${APP_PORT}:8080" \
    -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILES}" \
    -e SPRING_DATASOURCE_URL="jdbc:mariadb://${DB_HOST_CONTAINER}:${DB_PORT}/goldmanager" \
    -e SPRING_DATASOURCE_USERNAME="${DB_USER}" \
    -e SPRING_DATASOURCE_PASSWORD="${DB_PASS}" \
    -e APP_DEFAULT_USER="${APP_DEFAULT_USER}" \
    -e APP_DEFAULT_PASSWORD="${APP_DEFAULT_PASSWORD}" \
    "${APP_IMAGE}"
  APP_CONTAINER_STARTED=1
}

start_backend

wait_for_app() {
  local timeout_ms=${E2E_HEALTH_TIMEOUT_MS}
  local poll_ms=${E2E_HEALTH_POLL_MS}
  local max_attempts=$(( (timeout_ms + poll_ms - 1) / poll_ms ))
  local sleep_s
  sleep_s=$(awk "BEGIN { printf \"%.3f\", ${poll_ms}/1000 }")
  local ready=0
  for i in $(seq 1 ${max_attempts}); do
    if curl -sf "http://localhost:${APP_PORT}/api/health" >/dev/null 2>&1; then
      ready=1
      break
    fi
    [[ ${VERBOSE} -eq 1 ]] && log "waiting for app health (${i}/${max_attempts})" || true
    sleep "${sleep_s}"
  done
  if [[ ${ready} -ne 1 ]]; then
    log "App failed to become healthy after ${timeout_ms}ms"
    docker logs --tail 200 "${APP_CONTAINER}" || true
    return 1
  fi
  return 0
}

wait_for_app || exit 1

log "Backend is ready on http://localhost:${APP_PORT}"

# Run Playwright container
APP_BASE_URL="http://host.docker.internal:${APP_PORT}"
log "Running Playwright tests via ${PLAYWRIGHT_IMAGE} (base URL ${APP_BASE_URL}) ..."
set +e
RESULT=0

docker run --rm --user root ${EXTRA_HOST} --shm-size=1g \
  -v "${PWD}":/work -w /work \
  -e E2E_DB_HOST="${DB_HOST_CONTAINER}" -e E2E_DB_PORT="${DB_PORT}" \
  -e E2E_DB_WAIT_MS -e E2E_HEALTH_TIMEOUT_MS -e E2E_HEALTH_POLL_MS -e E2E_WEBSERVER_TIMEOUT_MS \
  -e VERBOSE="${VERBOSE}" \
  -e E2E_BASE_URL="${APP_BASE_URL}" \
  -e E2E_TEST_ARGS="${E2E_TEST_ARGS:-}" \
  "${PLAYWRIGHT_IMAGE}" bash -lc '
set -euo pipefail
[ "${VERBOSE:-0}" = "1" ] && set -x || true
cd /work/e2e
if [ -d node_modules/@playwright/test ]; then
  echo "[e2e] Using existing node_modules for Playwright."
else
  echo "[e2e] node_modules missing; running npm ci ..."
  npm ci
fi
mkdir -p /work/e2e/test-results /work/e2e/test-results-html
chown -R root:root /work/e2e/test-results /work/e2e/test-results-html || true

echo "[e2e] Running Playwright tests ..."
node node_modules/@playwright/test/cli.js test --config=playwright.no-server.config.ts ${E2E_TEST_ARGS:-}
'
RESULT=$?
set -e

if [[ ${RESULT} -ne 0 ]]; then
  log "Playwright tests failed (exit ${RESULT}). Backend logs (tail 200):"
  docker logs --tail 200 "${APP_CONTAINER}" || true
fi

exit ${RESULT}
