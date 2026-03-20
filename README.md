# eBikes Africa — Routing & Pricing Service

> Computes road distances, ETAs, and deterministic pricing quotes for orders before the agent assignment begins.

---

## Overview

The Routing & Pricing service owns route orchestration and the full pricing lifecycle for the eBikes Africa dispatch
platform. It wraps a self-hosted Valhalla routing engine to produce point-to-point routes and agent distance matrices,
and generates immutable pricing quotes scoped to global, organization, and branch commercial terms.

A committed `PricingQuote` is a prerequisite for an order entering `PENDING_ASSIGNMENT`.

**Owns:**

- Pricing plans, modifiers, and quotes
- Route and matrix orchestration via Valhalla
- Business vehicle class → engine costing profile mapping
- Haversine fallback when the routing engine is unavailable
- Scoped pricing resolution (`BRANCH → ORGANIZATION → GLOBAL`)

**Does not own:**

- Agent proximity search — belongs to Workforce
- Assignment strategy — belongs to Assignment
- Order lifecycle state transitions — belongs to Orders

---

## Platform Context

| Relationship | Service    | How                                                          |
|--------------|------------|--------------------------------------------------------------|
| Consumed by  | Orders     | `POST /quotes` — committed quote before `PENDING_ASSIGNMENT` |
| Consumed by  | Assignment | `POST /matrices` — distance/ETA for Workforce shortlist      |
| Depends on   | Valhalla   | Self-hosted routing engine for routes and matrices           |
| Auth via     | Keycloak   | JWT / OIDC — all endpoints require Bearer token              |

---

## Tech Stack

| Concern   | Technology                         |
|-----------|------------------------------------|
| Language  | Java 21                            |
| Framework | Spring Boot 3.x                    |
| Database  | PostgreSQL (Liquibase migrations)  |
| Messaging | RabbitMQ (outbox pattern)          |
| Auth      | Keycloak (JWT / OIDC)              |
| Cache     | Redis (short-lived preview quotes) |
| Routing   | Valhalla 3.6.x (self-hosted)       |

---

## Prerequisites

| Tool           | Version | Notes                               |
|----------------|---------|-------------------------------------|
| Java           | 21+     | Use SDKMAN: `sdk install java 21`   |
| Docker         | 20.10+  | Required for all local dependencies |
| Docker Compose | v2.0+   | Bundled with Docker Desktop         |
| Maven          | 3.9+    | Or use the included `mvnw.cmd`      |

---

## First-Time Setup

For secrets handling and upstream model, see [secrets](./documentation/SECRETS.md)

```bash
# 1. Copy environment template and configure
cp .env.example .env
# Edit .env — see inline comments for required values

# 2. Start infrastructure dependencies
docker compose up -d

# 3. Run the service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## API Reference

Full contract: `http://localhost:8093/swagger-ui/index.html`


> **Valhalla:** Routing requires Kenya OSM tile data. Tile build scripts are in the shared dev environment under
`templates/routing/valhalla/scripts/`. Run `fetch-kenya-extract.sh` then `build-tiles.sh` before starting Valhalla for
> the first time.

---

## Running Tests

```bash
# Full verify — mirrors the CI quality gate
./mvnw verify

# Unit tests only
./mvnw test
```

Coverage report: `target/site/jacoco/index.html`

---

## Database Schema

![Database schema](/documentation/ebikes-routing-database-schema.png)

---

## Environments & Deployment

| Environment  | Trigger                         | Image tag           |
|--------------|---------------------------------|---------------------|
| `dev`        | Push to `dev` (after CI passes) | `dev` + `sha-*`     |
| `staging`    | Push to `staging` (after CI)    | `staging` + `sha-*` |
| `production` | Release Please semver tag       | `vX.Y.Z` + `sha-*`  |

Images are published to AWS ECR. CI pipeline: `.github/workflows/`
