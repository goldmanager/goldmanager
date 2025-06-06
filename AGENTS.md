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

