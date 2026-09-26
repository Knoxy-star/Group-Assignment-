# DPDMS - Rushinga Provincial Disaster Monitoring and Management System

University of Zimbabwe, HCS201/HCC201/HAI201 (OOP), group assignment.
Due: 29 September 2026.

## Team

| Member | Owns |
|---|---|
| Marvelous | flood-service |
| Margret | drought-service |
| Portia | fire-service |
| Danai | zoonotic-disease-service |
| Knowledge | mining-accident-service |

Shared infrastructure (discovery-service, gateway, auth-service, report-service,
alert-service, dashboard-service) is built collaboratively.

## Stack

- Java 17, Spring Boot 3.3.4, Spring Cloud 2023.0.3
- MySQL 8 (one schema per service)
- RabbitMQ (async alert dispatch)
- React 18 + Vite (single-page front end, `frontend/`)
- Eureka (service discovery) + Spring Cloud Gateway (single entry point)
- Maven, single repo, multi-module

We started with Thymeleaf (see the note below on why) and had a working
page set for every service, but switched to React partway through so the
whole system would sit behind one consistent, more polished front end
instead of nine separate server-rendered page sets - the brief asks for
one front-end technology used consistently, and one SPA calling into all
nine REST APIs makes that a lot more convincing than nine copies of the
same three pages. All five hazards' submit/my-submissions/queue pages
are the same generic components, driven by a small per-hazard config
(`frontend/src/hazards/config.js`) - adding a hazard means adding one
config entry, not building a new page set. We kept it CORS-free the same
way the Thymeleaf pages were: the app never calls a service directly,
Vite's dev server proxies every `/<service-name>/**` path straight to
the gateway, so the browser only ever talks to one origin.

(Original reasoning for Thymeleaf, kept for context: it avoided a second
build pipeline and CORS setup on top of nine backend services within a
9-day timeline. React ended up being worth the extra setup once we had
more of the backend working and wanted the dashboard/map/reports to look
like a real product.)

## Project layout

```
dpdms/
  pom.xml                 <- parent, lists all modules
  discovery-service/      <- Eureka registry (port 8761)
  gateway/                <- API gateway, single entry point (port 8080)
  frontend/                <- React + Vite SPA (port 5173 in dev)
  docker-compose.yml       <- MySQL + RabbitMQ infra
  mysql-init/              <- creates one schema per service on first boot
  (more modules land here as they're built)
```

## Day 1 setup - do this once

1. Install Java 17, Maven, and Docker Desktop if you don't have them.
2. Clone the repo.
3. Start infrastructure:
   ```
   docker-compose up -d
   ```
   This starts MySQL (port 3306, creates all 9 schemas automatically)
   and RabbitMQ (port 5672, management UI at http://localhost:15672,
   login guest/guest).
4. Start the discovery service:
   ```
   cd discovery-service
   mvn spring-boot:run
   ```
   Confirm it's up: http://localhost:8761 should show the Eureka
   dashboard (no services registered yet - that's expected).
5. In a new terminal, start the gateway:
   ```
   cd gateway
   mvn spring-boot:run
   ```
   After ~10-20 seconds it should register itself with Eureka - refresh
   http://localhost:8761 and you should see GATEWAY listed as UP.

If both of those are running and visible in Eureka, Day 1 infrastructure
is working end to end and the next services can be added safely.

## Running order (once more services exist)

Always start in this order: `discovery-service` -> `gateway` -> everything
else (order among the rest doesn't matter, Eureka handles discovery).

## Frontend (React + Vite)

Everyone's page set (Thymeleaf templates, static JS, the little
per-service `WebController`) got removed once React was in - the SPA in
`frontend/` is the only UI now, for every service.

```
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The dev server proxies every
`/<service-name>/**` request straight to the gateway at
`http://localhost:8080` (see `vite.config.js`), so the backend must
already be running (discovery-service, gateway, and whichever hazard
services you want to exercise) - the frontend itself has no backend
logic of its own. Log in with any seeded account from the table below;
routing and the nav bar adapt automatically to the logged-in role.

`npm run build` produces a static `dist/` bundle for anyone who wants to
deploy the frontend separately (e.g. behind nginx or a static host);
that step isn't required for local development.

## auth-service (Day 2)

Issues JWTs, stores users with their role/hazard/ward scoping. Runs on
port 8081, reachable through the gateway at `/auth-service/api/auth/**`.

**Endpoints:**
- `POST /auth-service/api/auth/register` - create a user (username,
  password, fullName, role, hazard, ward, province - hazard/ward
  requirements depend on role, enforced server-side)
- `POST /auth-service/api/auth/login` - returns a JWT plus the user's
  role/hazard/ward

**Test accounts (seeded automatically on first startup, password for
all of them is `Password123!`):**

| Username | Role | Hazard | Ward |
|---|---|---|---|
| flood.recorder | WARD_RECORDER | FLOOD | Rushinga Ward 1 |
| drought.recorder | WARD_RECORDER | DROUGHT | Rushinga Ward 2 |
| fire.recorder | WARD_RECORDER | FIRE | Rushinga Ward 3 |
| zoonotic.recorder | WARD_RECORDER | ZOONOTIC_DISEASE | Rushinga Ward 4 |
| mining.recorder | WARD_RECORDER | MINING_ACCIDENT | Rushinga Ward 5 |
| flood.supervisor | PROVINCIAL_SUPERVISOR | FLOOD | - |
| drought.supervisor | PROVINCIAL_SUPERVISOR | DROUGHT | - |
| fire.supervisor | PROVINCIAL_SUPERVISOR | FIRE | - |
| zoonotic.supervisor | PROVINCIAL_SUPERVISOR | ZOONOTIC_DISEASE | - |
| mining.supervisor | PROVINCIAL_SUPERVISOR | MINING_ACCIDENT | - |
| national.viewer | NATIONAL_VIEWER | - | - |
| provincial.admin | PROVINCIAL_ADMIN | - | - |

Use these to test any hazard service's RBAC/scoping logic without
registering new users each time.

## Environment variables and secrets

See `.env.example` at the repo root. Every service falls back to a
documented dev-only default if an env var isn't set, so nobody needs
to configure anything just to run the system locally. Before final
submission, set real values (especially `JWT_SECRET`) as actual
environment variables - copy `.env.example` to `.env` (gitignored,
never commit it) as a reference for what each teammate needs to set.

**Critical constraint:** `JWT_SECRET` must be identical across
auth-service and gateway (and every future service that validates
tokens), or auth-service's tokens will be rejected at the gateway.
Don't let people set their own random value per machine.

## Conventions for hazard service authors

Once `flood-service` exists as the reference implementation, each hazard
service should:
- reuse the same shared incident fields (ward, district, province,
  occurredAt, reporterId, severity, status, latitude, longitude)
- implement the same approval state machine
  (PENDING -> APPROVED / REJECTED / CORRECTIONS_REQUESTED -> PENDING)
- enforce (ward, hazard) scoping for recorders and hazard scoping for
  supervisors independently, in the service itself - never rely on the
  gateway filter alone
- own its own MySQL schema (already created by mysql-init on first
  `docker-compose up`)

A per-hazard checklist (exact fields, exact DB table, exact DTOs) will be
issued once flood-service is done.

## mining-accident-service (Day 3) - REFERENCE IMPLEMENTATION

Runs on port 8082. This is the pattern the other four hazard services
copy. It has:
- REST API (`/mining-accident-service/api/incidents/**`) with full
  CRUD + approval workflow (approve / reject / request-corrections)
- RBAC/scoping enforced in the service layer via `HazardScopeGuard`
  (from the shared `common` module) - never trust the gateway alone
- Audit log recording every state transition

Its UI lives in the shared React frontend (`frontend/`), not in this
service - see "Frontend (React + Vite)" above.

**Try it end to end:**
1. Open `http://localhost:5173`, log in as `mining.recorder` /
   `Password123!` - it drops you straight on the mining-accident submit
   form (a recorder only ever sees their own hazard)
2. Submit an incident
3. Log out, log in as `mining.supervisor` / `Password123!` - this time
   you land on the mining-accident approval queue
4. Approve it, or try reject / request-corrections

**REST API (for Postman/testing):**

| Method | Path | Who |
|---|---|---|
| POST | `/api/incidents` | WARD_RECORDER (own ward/hazard) |
| GET | `/api/incidents` | any role - results scoped automatically |
| GET | `/api/incidents/{id}` | any role - scoped automatically |
| PUT | `/api/incidents/{id}` | recorder, own record, PENDING/CORRECTIONS_REQUESTED only |
| DELETE | `/api/incidents/{id}` | recorder, own record, not yet APPROVED |
| POST | `/api/incidents/{id}/approve` | supervisor, own hazard |
| POST | `/api/incidents/{id}/reject` | supervisor, own hazard, body: `{"notes": "..."}` |
| POST | `/api/incidents/{id}/request-corrections` | supervisor, own hazard, body: `{"notes": "..."}` |

Swagger UI: `http://localhost:8082/swagger-ui.html` (direct, or through
the gateway once routed).

## common module - shared library (NOT a running service)

`Role`, `Hazard`, `IncidentStatus`, `Severity`, `AuditAction` enums;
`BaseIncident` / `BaseAuditLog` (JPA MappedSuperclass - shared fields);
`RequestContext` + `RequestContextResolver` (reads the gateway's
X-User-* headers); `HazardScopeGuard` (the actual RBAC/scoping checks).

This is a compile-time dependency only - no hazard service calls
another hazard service over HTTP, each still has its own schema, own
REST API, own deployable jar. It's the same pattern as sharing a
DTO/utils jar in any production microservice system, and keeps the
RBAC logic identical and correct across all five services instead of
each person re-implementing (and possibly getting wrong) the same
scoping rules from scratch.

## Checklist for building another hazard service (copy mining-accident-service)

1. Copy the whole `mining-accident-service` folder, rename it to
   `<yourhazard>-service`, and do a project-wide rename of the Java
   package `zw.ac.uz.dpdms.mining` to `zw.ac.uz.dpdms.<yourhazard>`
   (IntelliJ: right-click the package > Refactor > Rename handles this
   safely, don't do it with find-replace on raw text).
2. In `pom.xml`: change `<artifactId>` to `<yourhazard>-service`.
3. In `application.yml`: change `server.port` to a free port (8083,
   8084, 8085, 8086 - agree as a team who takes which), change the
   datasource URL's database name to `<yourhazard>_db` (already
   created by `mysql-init`).
4. In your entity (e.g. `FloodIncident extends BaseIncident`): replace
   the 5 mining-specific fields with your hazard's 5 indicators from
   the brief, matching field types (number -> Integer/Double,
   categorical -> your own enum, yes/no -> Boolean).
5. In your service class: change `SERVICE_HAZARD` to your hazard's
   `Hazard` enum value (e.g. `Hazard.FLOOD`). That one line is what
   makes all the RBAC scoping apply correctly to your hazard - don't
   touch the `HazardScopeGuard` calls themselves, they're already
   correct.
6. Update your DTOs' hazard-specific fields to match your entity.
7. Add your hazard to `frontend/src/hazards/config.js` (one entry:
   slug, label, hazardEnum, servicePath, and your 5 fields with their
   types/options) - the submit form, tables and queue page pick it up
   automatically, nothing else in the frontend needs to change.
8. In `pom.xml` (root): uncomment your service's module line.
9. In gateway's `application.yml`: your route already exists (all 5
   hazard routes were pre-wired on Day 1) - nothing to change there.
10. Seed test accounts already exist for every hazard in auth-service's
    `DataSeeder` - use `<yourhazard>.recorder` / `<yourhazard>.supervisor`,
    password `Password123!`.
