# Agent Guidelines for goldmanager

This repository contains two sub-projects:

* **backend/** – Spring Boot application built with Gradle. Java 21 is required.
* **frontend/** – Vue 3 SPA built with Node (tested with Node 20).
  Use Node.js 20 for local development to match the CI environment.
* **e2e/** – Playwright end-to-end tests. Requires Node 20 and Java 21. Recommended to run inside the official Playwright Docker image via `./e2e/run-in-docker.sh` which installs JDK 21 in the container, maps the host DB, and sets extended timeouts for fresh DB initialization. HTML reports are written to `e2e/test-results-html/`; quick status is in `e2e/test-results/.last-run.json`.

## Response Protocol

Each answer must contain two clearly separated roles:

1. **Developer** – Implements the requested changes or
   explains how they were addressed. Prefix the section with
   `### Developer:`.
2. **Reviewer** – Reviews the developer's work for correctness, clarity,
   maintainability, best practices and potential issues. Prefix the section
   with `### Reviewer:`. If problems are found, explicitly request
   improvements and hand control back to the developer for the next
   iteration. If no Issues are found hand control to Test-Engineer
2. **Test-Engineer** – Checks the changes for necessary additional Playwright tests and ensures that playwright tests are running successfully if any code in backend or frontend is chaged. Prefix the section
   with `### Test-Engineer:`. If the user If problems are found, explicitly request
   improvements and hand control back to the developer for the next
   iteration.

Repeat this developer-reviewer-Test-Engineer cycle until the reviewer and/or Test-Engineer have no further
comments.
If the user requests launching of playwright tests without any code changes, the Test-Engineer runs them directly without actions from Developer or Reviewer.

## Build and Test Instructions

### Backend
* Use the Gradle wrapper to build and run tests:
  ```bash
  cd backend
  ./gradlew test
  ```
* Development server can be started with `./gradlew bootRun` after the database from `dev-env/compose.yaml` is running.
* Default admin user and default admin passwords can be set by running with java parameters -DAPP_DEFAULT_USER and -DAPP_DEFAULT_PASSWORD
** e.G. -DAPP_DEFAULT_USER=admin -DAPP_DEFAULT_PASSWORD=admin1Password!
* Since during the first usage of ./gradlew the gradle binaries are downloaded the first build in asession can take a few minutes. Wait a reasonable time accordingly.

### Frontend
* Install dependencies and run the linter or development server:
  ```bash
  cd frontend
  npm install
  npm run lint    # Lint source files
  npm run dev     # Dev server
  npm run build   # Production build
  ```
* The script `build.sh` builds the frontend and copies the output to
  `../backend/src/main/resources/static/` so the Spring Boot app can serve it.

### Docker Image
* The root `Dockerfile` builds both projects in a multi-stage process:
  1. Node stage to run `npm install`, create a CycloneDX SBOM for the
     frontend with `npx @cyclonedx/cyclonedx-npm`, and run `npm run build`.
  2. Gradle stage to build the backend using `gradle clean bootJar cyclonedxBom`
     and copy the frontend build into `src/main/resources/static`. The resulting
     backend SBOM is placed under `build/reports/application.cdx.json`.
  3. A `generate-sbom` stage based on `anchore/syft:latest` produces a combined
     image SBOM. The Syft binary resides at `/syft`, so it is invoked in exec
     form: `RUN ["/syft", "dir:.", "-o", "cyclonedx-json=image.cdx.json"]`.
  4. Final stage runs the generated JAR with Temurin JRE (port 8080/8443) and
     includes the backend, frontend and the combined image SBOM under `/bom`.
* Build with BuildKit enabled to create SBOM and provenance attestations:
  ```bash
  DOCKER_BUILDKIT=1 docker build --sbom=goldmanager.sbom.json --provenance=mode=max -t goldmanager .
  ```

## Development Database
A MariaDB instance for local development is defined in
`backend/dev-env/compose.yaml`. Start it with Docker Compose before running the
backend locally:

```bash
docker compose -f backend/dev-env/compose.yaml up -d
```

## Testing Requirement for Pull Requests
After modifying backend or frontend code, run the following checks before
committing:
When backend functions are added or changed, corresponding unit or integration tests must be created or updated.

1. `./gradlew test` inside `backend/`
2. `npm run lint` inside `frontend/`
3. `npm run test` inside `frontend/`

Include any relevant outputs in the PR description.

### E2E (Playwright)
- Location: `e2e/`.
- Use shell script to run in docker. This also sets up playwright in a docker container and runs a build of backend and frontend.
  - `./e2e/run-in-docker.sh`
- The Playwright config ensures DB readiness, builds frontend, builds backend JAR, and starts the Spring Boot app before tests at `http://localhost:8080`.
 
### Preferred E2E Execution for Agents (network-restricted)

Use the helper script `e2e/run-in-docker-agent.sh`. It runs tests fully inside the prebuilt Playwright Docker image while reusing the already-built backend JAR and the host E2E MariaDB. This avoids in-container downloads and host browser dependencies.

Prerequisites:
- E2E DB is up on the host (MariaDB): `docker compose -f e2e/dev-db/compose.yaml up -d`
- Backend JAR is already built on the host at `backend/build/libs/*.jar` (non `-plain`). If not present, build it outside the restricted agent: `cd backend && ./gradlew bootJar`.
- Playwright image exists: `goldmanager/e2e-playwright:local` (build once if missing: `docker build -f e2e/Dockerfile -t goldmanager/e2e-playwright:local .`).
- E2E dependencies installed once on host (to avoid network in container): `cd e2e && npm ci`
  - Note: If you previously built the backend inside a container, `backend/build` files may be owned by root. If `--build-jar` fails with permission errors, fix ownership: `sudo chown -R "$USER":"$USER" backend/build`.

Run tests (all browsers) from the repo root:

```
bash ./e2e/run-in-docker-agent.sh
```

Examples with extra args (passed to Playwright):

```
# Only Chromium
bash ./e2e/run-in-docker-agent.sh -- --project=chromium

# Single spec with UI
bash ./e2e/run-in-docker-agent.sh -- tests/user-management.spec.ts --ui

# Build backend JAR first, then run all tests
bash ./e2e/run-in-docker-agent.sh --build-jar

# Fix permissions on backend/build and report dirs, then run Chromium only
bash ./e2e/run-in-docker-agent.sh --fix-perms -- --project=chromium

# Only fix backend/build permissions, then run Chromium
bash ./e2e/run-in-docker-agent.sh --fix-perms-backend -- --project=chromium

# Only fix report directories, then run Chromium
bash ./e2e/run-in-docker-agent.sh --fix-perms-reports -- --project=chromium

# Force a full clean E2E DB (down -v; up -d) before tests
bash ./e2e/run-in-docker-agent.sh --clean-db
```

Notes:
- The config `e2e/playwright.no-server.config.ts` disables Playwright’s webServer and uses the container-local app at `http://localhost:8080`. You can override `baseURL` by setting `E2E_BASE_URL` in that config if needed.
- The script ensures test report directories are writable on the bind mount to avoid EACCES on `.last-run.json`.
- E2E DB management:
  - Dedicated MariaDB runs via `e2e/dev-db/compose.yaml` (default port 3317).
  - The Docker runner ensures a clean DB before tests. Default is a fast SQL reset (drop/recreate `goldmanager` DB for user `myuser`). Set `E2E_DB_RESET_MODE=compose` to perform a full `docker compose down -v && up -d` reset.
- Image versioning:
  - The E2E Docker image preinstalls Playwright browsers. Override version via env, e.g.: `PLAYWRIGHT_VERSION=1.55.0 ./e2e/run-in-docker.sh`.
  - The build arg `PLAYWRIGHT_VERSION` is passed to `e2e/Dockerfile` to align the image with the test runner.
- Reliability env vars for slow first runs/fresh DBs:
  - `E2E_DB_WAIT_MS` (default 360000): Max wait for MariaDB TCP readiness.
  - `E2E_HEALTH_TIMEOUT_MS` (default 600000): Max wait for app health endpoint.
  - `E2E_WEBSERVER_TIMEOUT_MS` (default 720000): Playwright webServer readiness timeout.
- Example: `tests/login.spec.ts` performs a login with default credentials.
 - Readiness is based on `GET /api/health` which is public and returns `{ "status": "ok" }`.
 - Config:
   - DB overrides: `E2E_DB_HOST` (default `localhost`), `E2E_DB_PORT` (default `3307`).
   - Frontend install prefers `npm ci` when `package-lock.json` is present.
    - Docker auto-start for the dev DB runs only when the DB host is local (`localhost`, `127.0.0.1`, or `::1`).

## Commit Guidelines
Use concise commit messages that start with a short imperative summary.
Provide additional context in the body if needed.

# Authentication Configurations
The Spring Boot application defines two Security Configurations:
1. `DefaultSecurityConfiguration` (profiles `default` and `dev`)
2. `TestSecurityConfiguration` (profile `test`)

`DefaultSecurityConfiguration` applies the same rules for both the default and
development profiles—sessions are stateless, CSRF and HTTP basic auth are
disabled, and the `JwtAuthenticationFilter` is inserted. `DevWebConfig` adds
CORS settings for development so that a UI dev server like Quasar can reach the
REST API. `TestSecurityConfiguration` simplifies
authentication for integration tests and is activated when running with the
`test` profile.

# Repository Instructions

- `backend/src/test/resources/application-test.properties` configures the in-memory H2 database used during tests.
- When a test class uses `@ActiveProfiles("test")`, Spring Boot loads `TestSecurityConfiguration` to simplify authentication for testing.
- Run `./gradlew test` from the `backend` directory to execute all backend tests.
  
## Documentation Guidelines
Any documentation (especially javadoc and comments in code) shall be written in english.
Also documentation in AGENTS.md and any available README.md files shall be written in english.

## REST API Documentation
The file `docs/rest_api.md` explains the available REST endpoints and how the Vue frontend consumes them. Keep this document up to date whenever the API or its usage changes.

## Third-Party License Tracking
Changes to dependencies in `backend/build.gradle` or `frontend/package.json` require an update to `docs/third_party_licenses.md`. Keep the table of libraries and licenses in that file up to date so license information remains accurate.

## AGENTS.md Maintenance

The instructions in this file must accurately reflect the repository. Whenever
code or project structure described here changes, update **AGENTS.md** to match
the new reality. Document fundamental changes, such as build steps,
dependencies or folder layout, so future tasks have up-to-date guidance. If a
contradiction between the repository and this document is found, correct this
file immediately. Add any newly discovered information that helps developers
understand or maintain the project.
