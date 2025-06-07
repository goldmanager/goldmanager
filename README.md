# About
This application manages your gold and silver assets and calculates their prices
 by the provided material price.
# Project structure
The project contains two directories containing backend and frontend components
- backend/ Contains the backend, implemented in Spring-Boot.
- frontend/ Contains the frontend implemented in Vue3.
# Docker
You can build the application as Docker image by using the Dockerfile in the root directory.
Please see also https://github.com/goldmanager/goldmanager-dockercompose for an example on usage with docker compose

## Data Import

Data import is processed asynchronously. Use `POST /api/dataimport/import` with a JSON body containing `data` and `password`. The request returns HTTP `202 Accepted`.
The current status can be retrieved via `GET /api/dataimport/status` returning `IDLE`, `RUNNING`, `SUCCESS` or `FAILED`. If another import is triggered while a job
is already running the service responds with HTTP `409 Conflict`.
