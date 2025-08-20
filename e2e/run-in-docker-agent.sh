#!/usr/bin/env bash
set -euo pipefail

# Agent-friendly Playwright E2E runner
# - Reuses host-built backend JAR
# - Uses host E2E MariaDB via host.docker.internal
# - Runs tests inside Playwright Docker image against a no-webserver config
# - Avoids network installs by reusing existing e2e/node_modules

IMAGE="${PLAYWRIGHT_IMAGE:-goldmanager/e2e-playwright:local}"
COMPOSE_FILE="e2e/dev-db/compose.yaml"
DB_HOST="host.docker.internal"
DB_PORT="${E2E_DB_PORT:-3317}"

echo "[agent-e2e] Using image: ${IMAGE}"

# Ensure E2E DB is up on host
echo "[agent-e2e] Ensuring E2E DB is running via ${COMPOSE_FILE} ..."
docker compose -f "${COMPOSE_FILE}" up -d >/dev/null

# Verify backend JAR exists
JAR=$(ls -1 backend/build/libs/*.jar 2>/dev/null | grep -v -- '-plain.jar' | head -n1 || true)
if [ -z "${JAR}" ]; then
  echo "[agent-e2e] ERROR: Backend JAR not found at backend/build/libs. Build it first:"
  echo "             cd backend && ./gradlew bootJar"
  exit 1
fi

# Build Playwright image if needed
if ! docker image inspect "${IMAGE}" >/dev/null 2>&1; then
  echo "[agent-e2e] Building Playwright image ..."
  docker build -f e2e/Dockerfile -t "${IMAGE}" .
fi

# Pass-through any extra args to 'npm test --'
E2E_TEST_ARGS="$*"

echo "[agent-e2e] Starting tests in Docker (this may take ~20–40s) ..."
docker run --rm --user root --add-host=host.docker.internal:host-gateway --shm-size=1g \
  -e SPRING_DATASOURCE_URL="jdbc:mariadb://${DB_HOST}:${DB_PORT}/goldmanager" \
  -v "${PWD}":/work -w /work \
  -p 8080:8080 \
  "${IMAGE}" bash -lc '
set -euo pipefail
cd /work/e2e
# Require existing node_modules to avoid network installs in restricted envs
if [ ! -d node_modules/@playwright/test ]; then
  echo "[agent-e2e] ERROR: e2e/node_modules missing. Please run: (cd e2e && npm ci)" >&2
  exit 2
fi

mkdir -p /work/e2e/test-results /work/e2e/test-results-html
chown -R root:root /work/e2e/test-results /work/e2e/test-results-html || true

cd /work
JAR=$(ls -1 backend/build/libs/*.jar | grep -v -- -plain.jar | head -n1)
JAVA_OPTS="-Dspring.profiles.active=dev -DAPP_DEFAULT_USER=admin -DAPP_DEFAULT_PASSWORD=admin1Password!"
(java $JAVA_OPTS -jar "$JAR" > /work/e2e/app.log 2>&1 &) 

echo "[agent-e2e] Waiting for app health at http://localhost:8080/api/health ..."
for i in $(seq 1 120); do
  curl -sf http://localhost:8080/api/health >/dev/null 2>&1 && break || sleep 1
done

cd /work/e2e
echo "[agent-e2e] Running Playwright tests ..."
npm test -- --config=playwright.no-server.config.ts ${E2E_TEST_ARGS}
'

exit $?

