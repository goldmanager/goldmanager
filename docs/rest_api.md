# Goldmanager REST API Overview

This document provides an overview of the backend REST API and how it is consumed by the Vue UI.

## Overview

The backend exposes a set of endpoints under the `/api` prefix. They allow management of materials, items and prices of precious metals. Authentication is handled with JWT tokens. During development the `dev` Spring profile enables CORS to allow local UI servers.

## Authentication

The `AuthController` offers `/api/auth/login` to obtain a JWT token. The token must be included as `Authorization: Bearer <token>` in subsequent requests. `/api/auth/refresh` provides token refresh and `/api/auth/logoutuser` clears the server side session.

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
| User Service | `/api/userService` | endpoints for managing users |

The controllers reside under `backend/src/main/java/com/my/goldmanager/controller/`.

## Integration with the UI

The Vue frontend uses Axios (`src/axios.js`) configured with the base URL `/api/`. The Axios instance automatically attaches the stored JWT token and refreshes it when needed. Components invoke the API through this instance to retrieve or modify data. Routes defined in `src/router/index.js` guard access and redirect to `/login` when no token is present.

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
npm run serve
```

## Example Workflow

1. Authenticate via POST `/api/auth/login`.
2. Use the received token to create a material with POST `/api/materials`.
3. Add an item referencing that material with POST `/api/items`.
4. Fetch current prices using GET `/api/prices`.
5. View the data in the UI under `/items` or `/metals` routes.

Keep this document updated whenever endpoints or authentication behaviour change so that it remains a reliable reference.

