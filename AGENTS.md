# Agent Guidelines for goldmanager

This repository contains two sub-projects:

* **backend/** – Spring Boot application built with Gradle. Java 21 is required.
* **frontend/** – Vue 3 SPA built with Node (tested with Node 16).

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
  1. Node stage to run `npm install` and `npm run build` under `frontend/`.
  2. Gradle stage to build the backend using `gradle clean bootJar` and
     copy the frontend build into `src/main/resources/static`.
  3. Final stage runs the generated JAR with Temurin JRE (port 8080/8443).
* Build with:
  ```bash
  docker build -t goldmanager .
  ```

## Development Database
A MariaDB instance for local development is defined in
`backend/dev-env/compose.yaml`. Start it with Docker Compose before running the
backend locally.

## Testing Requirement for Pull Requests
After modifying backend or frontend code, run the following checks before
committing:

1. `./gradlew test` inside `backend/`
2. `npm run lint` inside `frontend/`

Include any relevant outputs in the PR description.

# Authentication Configurations
The Spring Boot Application defines 3 Security Configurations:
1. In main code DefaultSecurityConfiguration and DevSecurityConfiguration
2. In test code TestSecurityConfiguration

DefaultSecurityConfiguration is intended for use in productive and test environments when running with the default profile. DevSecurityConfiguration is intended for use in developer environments, e.G. when launching in Eclipse IDE or IntelliJ with profile dev.
TestSecurityConfiguration is intended for use in SpringBootTests and is activated by profile test.

# Repository Instructions

- `backend/src/test/resources/application-test.properties` configures the in-memory H2 database used during tests.
- When a test class uses `@ActiveProfiles("test")`, Spring Boot loads `TestSecurityConfiguration` to simplify authentication for testing.
- Run `./gradlew test` from the `backend` directory to execute all backend tests.
