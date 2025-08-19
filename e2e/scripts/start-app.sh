#!/usr/bin/env bash
set -euo pipefail

# This script prepares and starts the GoldManager application for Playwright E2E tests.
# Requirements:
# - Node.js 20+
# - Java 21+
# - MariaDB from backend/dev-env/compose.yaml must be running
#   Start it with: docker compose -f backend/dev-env/compose.yaml up -d

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="${SCRIPT_DIR}/.."

echo "[e2e] Building frontend and syncing to backend static resources..."
(
  cd "${REPO_ROOT}/../frontend"
  ./build.sh
)

echo "[e2e] Building backend bootJar..."
(
  cd "${REPO_ROOT}/../backend"
  ./gradlew clean bootJar --no-daemon
)

JAR_PATH=$(ls -1 "${REPO_ROOT}/../backend/build/libs"/*.jar | head -n 1)
if [[ -z "${JAR_PATH}" || ! -f "${JAR_PATH}" ]]; then
  echo "[e2e] ERROR: Could not locate backend JAR under backend/build/libs" >&2
  exit 1
fi

echo "[e2e] Starting backend from ${JAR_PATH} on http://localhost:8080 ..."
exec java \
  -Dspring.profiles.active=dev \
  -DAPP_DEFAULT_USER=admin \
  -DAPP_DEFAULT_PASSWORD=admin1Password! \
  -jar "${JAR_PATH}"

