# Agent Guidelines for goldmanager

This repository contains two sub-projects:

* **backend/** – Spring Boot application built with Gradle. Java 21 is required.
* **frontend/** – Vue 3 SPA built with Node (tested with Node 16).
  Use Node.js 16 for local development to match the CI environment.

## Build and Test Instructions

### Backend
* Use the Gradle wrapper to build and run tests:
  ```bash
  cd backend
  ./gradlew test
  ```
* Development server can be started with `./gradlew bootRun` after the database from `dev-env/compose.yaml` is running.

### Frontend
* Install dependencies and run the linter or development server:
  ```bash
  cd frontend
  npm install
  npm run lint    # Lint source files
  npm run serve   # Dev server
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
  3. Final stage runs the generated JAR with Temurin JRE (port 8080/8443) and
     includes both SBOM files under `/bom`.
* Build with:
  ```bash
  docker build -t goldmanager .
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

Include any relevant outputs in the PR description.

## Commit Guidelines
Use concise commit messages that start with a short imperative summary.
Provide additional context in the body if needed.

# Authentication Configurations
The Spring Boot application defines three Security Configurations:
1. `DefaultSecurityConfiguration` (profile `default`)
2. `DevSecurityConfiguration` (profile `dev`)
3. `TestSecurityConfiguration` (profile `test`)

`DefaultSecurityConfiguration` and `DevSecurityConfiguration` contain the same
rules—sessions are stateless, CSRF and HTTP basic auth are disabled, and the
`JwtAuthenticationFilter` is inserted. They only differ by the active profile.
`DevWebConfig` adds CORS settings for development so that a UI dev server like
Quasar can reach the REST API. `TestSecurityConfiguration` simplifies
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
