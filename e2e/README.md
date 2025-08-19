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
  - `E2E_DB_PORT` (default: `3307`)
- Frontend install uses `npm ci` when `package-lock.json` exists; otherwise falls back to `npm install`.
 - When `E2E_DB_HOST` is `localhost`, `127.0.0.1`, or `::1`, the launcher will attempt to auto-start MariaDB via Docker Compose; for non-local hosts it will skip auto-start and only wait for readiness.

## Notes
- The first Gradle run may take a few minutes to download wrappers and dependencies.
- Ensure MariaDB is up before running tests; otherwise the backend cannot start with the `dev` profile.
