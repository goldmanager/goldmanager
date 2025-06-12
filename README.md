# About
This application manages your gold and silver assets and calculates their prices
 by the provided material price.
# Project structure
The project contains two directories containing backend and frontend components
- backend/ Contains the backend, implemented in Spring-Boot.
- frontend/ Contains the frontend implemented in Vue3.
# Docker
You can build the application as Docker image by using the Dockerfile in the root directory.
To generate supply chain attestations (SBOM and provenance) enable BuildKit and run:

```bash
DOCKER_BUILDKIT=1 docker build --sbom=goldmanager.sbom.json --provenance=mode=max -t goldmanager .
```

The resulting files `goldmanager.sbom.json` and `provenance.json` are written to the current directory. The image also contains a combined SBOM at `/bom/image.cdx.json`.
Please see also https://github.com/goldmanager/goldmanager-dockercompose for an example on usage with docker compose

## Development setup

The frontend is built and tested with Node.js 20. Install dependencies with `npm install` inside `frontend/` before running `npm run lint` and `npm run test` or starting the dev server.
The backend requires Java 21 and can be tested with `./gradlew test` in the `backend/` directory.

## Configuration

The size of encrypted export data that can be processed during import is limited. You can adjust the limit via the property `com.my.goldmanager.service.dataexpimp.maxEncryptedDataSize` in `backend/src/main/resources/application.properties`. The default value is `52428800` bytes (50 MB).

## Data Import
Data import is processed asynchronously. Use `POST /api/dataimport/import` with a JSON body containing `data` and `password`. The request returns HTTP `202 Accepted`.
The current status can be retrieved via `GET /api/dataimport/status` returning `IDLE`, `RUNNING`, `SUCCESS` or `FAILED`. If another import is triggered while a job
is already running the service responds with HTTP `409 Conflict`.

# License
This project is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

# Third-Party Licenses
A list of key dependencies and their licenses can be found in [docs/third_party_licenses.md](docs/third_party_licenses.md). All dependencies were reviewed for compatibility with the Apache License 2.0.

