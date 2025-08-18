# Agent Guidelines for goldmanager

This repository contains two sub-projects:

* **backend/** – Spring Boot application built with Gradle. Java 21 is required.
* **frontend/** – Vue 3 SPA built with Node (tested with Node 20).
  Use Node.js 20 for local development to match the CI environment.

## Response Protocol

Each answer must contain two clearly separated roles:

1. **Developer** ("Entwickler") – Implements the requested changes or
   explains how they were addressed. Prefix the section with
   `### Entwickler:`.
2. **Reviewer** – Reviews the developer's work for correctness, clarity,
   maintainability, best practices and potential issues. Prefix the section
   with `### Reviewer:`. If problems are found, explicitly request
   improvements and hand control back to the developer for the next
   iteration.

Repeat this developer-reviewer cycle until the reviewer has no further
comments.

## Build and Test Instructions

### Backend
* Use the Gradle wrapper to build and run checks (includes tests and coverage verification):
  ```bash
  cd backend
  ./gradlew check
  ```
* Development server can be started with `./gradlew bootRun` after the database from `dev-env/compose.yaml` is running.
* Default admin user and default admin passwords can be set by running with java parameters -DAPP_DEFAULT_USER and -DAPP_DEFAULT_PASSWORD
** e.G. -DAPP_DEFAULT_USER=admin -DAPP_DEFAULT_PASSWORD=admin1Password!
* Since during the first usage of ./gradlew the gradle binaries are downloaded the first build in asession can take a few minutes. Wait a reasonable time accordingly.

#### Mutation Testing (PIT)
PIT is integrated into the backend build and scoped to core logic packages for faster runs (`service`, `encoder`, `pricecollector`, `service.util`, `service.dataexpimp`). By default it does not run with `check` to keep the feedback loop fast. To execute mutation tests:

```bash
cd backend
# Run PIT standalone
./gradlew pitest

# Or include PIT as part of check via a property
./gradlew check -PrunPitest

# Reports are generated under:
#   backend/build/reports/pitest/index.html
```

Notes:
- PIT runs JUnit 5 tests. It excludes Spring configuration and controller classes by default to avoid mutating framework wiring.
- Scope: Only core logic classes are mutated by default (services, utilities, data export/import, encoder, price collector). Adjust `targetClasses` in `backend/build.gradle` if you want broader coverage.
- If you need to skip all tests entirely, you can still use `-PskipTests` which also prevents PIT from running during `check`.

PIT Gradle plugin version:
- The project uses the `info.solidsoft.pitest` Gradle plugin. If you encounter a Gradle deprecation warning about `ReportingExtension.getBaseDir()`, update the plugin in `backend/build.gradle` to the latest available version and rerun the build. Newer versions use Gradle's `getBaseDirectory()` and remove the warning.

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

1. `./gradlew check` inside `backend/` (runs tests and enforces JaCoCo coverage)
   - Optionally include mutation tests: `./gradlew check -PrunPitest`
2. `npm run lint` inside `frontend/`
3. `npm run test:coverage` inside `frontend/` (generates coverage report and enforces thresholds)

Include any relevant outputs in the PR description.

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
- Run `./gradlew check` from the `backend` directory to execute tests and coverage verification.
  
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
