# Goldmanager REST API Overview

This document provides an overview of the backend REST API and how it is consumed by the Vue UI.

## Overview

The backend exposes a set of endpoints under the `/api` prefix. They allow management of materials, items and prices of precious metals. Authentication is handled with JWT tokens. During development the `dev` Spring profile enables CORS to allow local UI servers.

## Authentication

The `AuthController` offers `/api/auth/login` to obtain a JWT token. The token is
delivered only as an HttpOnly cookie named `jwt-token`. A CSRF token is also
issued as a cookie named `XSRF-TOKEN`. If the cookie is missing the endpoint
`/api/auth/csrf` can be called once to obtain it. Axios reads this cookie and sends the
value in the `X-XSRF-TOKEN` header automatically. `/api/auth/refresh` refreshes
the JWT cookie and returns a JSON body with the new expiration data while
`/api/auth/logoutuser` clears the session cookie.

## Main Endpoints

| Resource | Base URL | Sample operations |
|----------|---------|------------------|
| Items | `/api/items` | `POST`, `PUT /{id}`, `GET /{id}`, `DELETE /{id}`, list all via `GET` |
| Item Types | `/api/itemTypes` | CRUD operations similar to items |
| Item Storages | `/api/itemStorages` | CRUD operations |
| Materials | `/api/materials` | CRUD operations |
| Material History | `/api/materialHistory` | `GET /{materialId}` to list history |
| Units | `/api/units` | `GET`, `GET /{name}`, `POST`, `PUT /{name}`, `DELETE /{name}` |
| Prices | `/api/prices` | `GET` list, `GET /item/{id}`, `GET /material/{id}`, `GET /itemStorage/{id}`, grouping endpoints |
| Price History | `/api/priceHistory` | `GET /{materialId}` with optional `startDate` and `endDate` |
| Data Import | `/api/dataimport` | `POST /import` to start an import (returns `202`), `GET /status` for the current job status |
| Data Export | `/api/dataexport` | `POST /export` to start an export (returns `202`), `GET /status` for the current job status, `GET /download` to retrieve the data |
| User Service | `/api/userService` | endpoints for managing users |

The controllers reside under `backend/src/main/java/com/my/goldmanager/controller/`.

## Data Import

Data import is asynchronous. Use `POST /api/dataimport/import` with a JSON body containing `data` and
`password`. The request returns HTTP `202 Accepted` when the import started. Poll
`GET /api/dataimport/status` to check the current job status. This endpoint is publicly accessible so the
UI can determine if an import is in progress before logging in. The response contains the job status and
an optional message. Possible states are `IDLE`, `RUNNING`, `SUCCESS`, `FAILED` and `PASSWORD_ERROR`.
If another import is triggered while one is already running the service responds with HTTP `409 Conflict`.

## Data Export

Data export mirrors the import workflow. Trigger the job with `POST /api/dataexport/export` providing
`password` in the JSON body. The response is HTTP `202 Accepted` once the export started. Poll
`GET /api/dataexport/status` until it returns `SUCCESS`. The status payload also includes a message and
may report `PASSWORD_ERROR` when the supplied password is invalid. Download the result via
`GET /api/dataexport/download`. Starting a new export while one is running yields HTTP `409 Conflict`.

## Integration with the UI

The Vue frontend uses Axios (`src/axios.js`) configured with the base URL `/api/`.
It automatically sends the `jwt-token` and `XSRF-TOKEN` cookies with each
request and refreshes the session when needed. Components invoke the API through
this instance to retrieve or modify data. Routes defined in `src/router/index.js`
guard access and redirect to `/login` when no username is stored.

To start a local development environment:

```bash
# Start database
docker compose -f backend/dev-env/compose.yaml up -d

# Run backend
cd backend
./gradlew bootRun

# Run frontend
cd ../frontend
npm install
npm run dev
```

## Example Workflow

1. Authenticate via POST `/api/auth/login`.
2. The browser stores the `jwt-token` cookie automatically. Use it to create a material with POST `/api/materials`.
3. Add an item referencing that material with POST `/api/items`.
4. Fetch current prices using GET `/api/prices`.
5. View the data in the UI under `/items` or `/metals` routes.

Keep this document updated whenever endpoints or authentication behaviour change so that it remains a reliable reference.

