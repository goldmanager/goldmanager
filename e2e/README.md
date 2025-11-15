# End-to-End (E2E) Tests with Playwright

This folder contains Playwright-based E2E tests for GoldManager. Tests run against the Spring Boot app serving the built Vue frontend on `http://localhost:8080`.

## Prerequisites
- Node.js 20+
- Java 21+
- Docker (to start MariaDB for development)

The launcher tries to auto-start the dev MariaDB using Docker Compose. If Docker
is not available, start it manually:

```bash
docker compose -f backend/dev-env/compose.yaml up -d
```

## Install and Run

```bash
cd e2e
npm install
npx playwright install

# Run tests (headless)
npm test

# Run the Playwright UI test runner
npm run test:ui
```

The Playwright config will:
- Ensure MariaDB is running (starts via Docker if possible).
- Build the frontend and copy it into the backend static resources.
- Build the backend JAR with Gradle.
- Start the Spring Boot app and wait for `http://localhost:8080/api/health`.

Default credentials are passed via JVM args: `admin` / `admin1Password!`.

## Example Test
- `tests/login.spec.ts` performs a login using the default credentials and verifies the Prices page.

## Health Endpoint
- `GET /api/health` is public and returns only `{ "status": "ok" }` with HTTP 200.
- It exposes no sensitive information and is used by the E2E launcher for readiness.

## Configuration
- DB host/port overrides for local setups:
  - `E2E_DB_HOST` (default: `localhost`)
  - `E2E_DB_PORT` (default: `3317` when using the E2E Docker DB; `3307` when using the dev DB)
- Frontend install uses `npm ci` when `package-lock.json` exists; otherwise falls back to `npm install`.
 - When `E2E_DB_HOST` is `localhost`, `127.0.0.1`, or `::1`, the launcher will attempt to auto-start MariaDB via Docker Compose; for non-local hosts it will skip auto-start and only wait for readiness.

## Run in Docker (recommended)

To avoid host dependency issues and standardize the environment, run the tests via the single entrypoint script:

```bash
./e2e/run-in-docker.sh
```

What the script does:
- Ensures the dedicated MariaDB from `e2e/dev-db/compose.yaml` is running and performs a fast SQL drop/recreate of the `goldmanager` schema (set `E2E_DB_RESET_MODE=compose` for a full `down -v` reset).
- Builds the backend Docker image `goldmanager:latest` from the repo root when no target image is provided, then starts that container on host port 8080 with the dev profile and default user credentials exposed via env.
- The runtime stage uses the Debian-based Temurin JRE to avoid the PaX/Grsecurity restrictions that prevented the Alpine variant from starting on hardened hosts.
- Launches the Playwright Docker image (prebuilt locally as `goldmanager/e2e-playwright:local`) which installs JDK 21, reuses `e2e/node_modules` when present, and runs the tests against `E2E_BASE_URL=http://host.docker.internal:8080`.

Useful flags:
- `--app-image <image[:tag]>` – use an already-built backend image instead of rebuilding `goldmanager:latest`.
- `--clean-db` – force `docker compose down -v` before `up -d` for the E2E database stack.
- `--fix-perms`, `--fix-perms-backend`, `--fix-perms-reports` – repair ownership for `backend/build` and/or the Playwright report folders when previous container runs created root-owned files.
- `--verbose` – enables extra logging in the Playwright container and prints the backend logs on failure.
- Extra Playwright CLI args go after `--`, e.g. `./e2e/run-in-docker.sh -- --project=chromium`.

Timeout helpers for cold starts:
- `E2E_DB_WAIT_MS` (default 360000) for DB TCP readiness.
- `E2E_HEALTH_TIMEOUT_MS` (default 600000) for the application health endpoint.
- `E2E_WEBSERVER_TIMEOUT_MS` (default 720000) to allow Playwright web server readiness.

Playwright version:
- The Docker image builds with a parameterized Playwright version and preinstalls matching browsers/deps. Override with env var when needed:
  - `PLAYWRIGHT_VERSION=1.55.0 ./e2e/run-in-docker.sh`
  - The script passes the version into `e2e/Dockerfile` via `--build-arg PLAYWRIGHT_VERSION=...`.

## Results & Reports

- Quick status: the last run summary is written to `e2e/test-results/.last-run.json`.
  - Example: `cat e2e/test-results/.last-run.json`
- HTML report: generated under `e2e/test-results-html/`.
  - Open via Playwright: `cd e2e && npx playwright show-report ./test-results-html` or `npm run report`
- JSON report: `e2e/test-results/report.json`.
- Traces/videos: traces are collected for all tests, videos/screenshots are kept on failures.

## Notes
- The first Gradle run may take a few minutes to download wrappers and dependencies.
- Ensure MariaDB is up before running tests; otherwise the backend cannot start with the `dev` profile.
