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
- React + Vite frontend (`frontend/`) - talks to every service through
  the gateway at relative paths like `/flood-service/api/incidents`, so
  there's zero CORS configuration in the backend
- Eureka (service discovery) + Spring Cloud Gateway (single entry point)
- Maven, single repo, multi-module

The project originally used server-rendered Thymeleaf pages; those were
replaced with the React + Vite SPA in `frontend/` (see commit history).
If you find any doc or comment still describing Thymeleaf pages, it's
stale - the frontend is now the only UI.

## Project layout

```
dpdms/
  pom.xml                 <- parent, lists all modules
  discovery-service/      <- Eureka registry (port 8761)
  gateway/                <- API gateway, single entry point (port 8080)
  frontend/               <- React + Vite SPA (dev server port 5173)
  docker-compose.yml       <- MySQL + RabbitMQ infra
  mysql-init/              <- creates one schema per service on first boot
  notification-service/   <- unfinished/unused scaffold (Twilio), not
                              wired into the gateway or started by anyone;
                              ignore unless you're picking it up
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

## Running the frontend

```
cd frontend
npm install
npm run dev
```
Opens at http://localhost:5173. Vite proxies every `/xxx-service/**`
request straight through to the gateway (port 8080), so the gateway and
all backing services need to be up first. `npm run build` produces a
production bundle in `frontend/dist/` (gitignored, build output only).

## Running order (once more services exist)

Always start in this order: `discovery-service` -> `gateway` -> everything
else (order among the rest doesn't matter, Eureka handles discovery).

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

## Database

MySQL runs as the `mysql` container defined in `docker-compose.yml`.
Its data is **not** a file inside this repo - it lives in a Docker-
managed named volume (`mysql_data`), which Docker stores under its own
data directory (inside the Docker Desktop VM on Windows/Mac). This is
the standard, portable way to persist a containerized database: every
teammate gets the same behavior regardless of where they cloned the
repo, and nothing under `dpdms/` depends on an absolute path on anyone's
machine.

- Starts fresh on a new machine the first time `docker-compose up -d`
  runs: `mysql-init/01-create-databases.sql` creates all 9 schemas and
  the shared `dpdms` app user automatically.
- To wipe the database and start over: `docker-compose down -v` (removes
  the `mysql_data` volume too).
- Every service connects to `jdbc:mysql://localhost:3306/<name>_db`
  (see each service's `src/main/resources/application.yml`) - no
  hardcoded host paths anywhere in the config.

### Importing the included database dump

`dpdms-dump.sql` (repo root) is a `mysqldump` snapshot of all 9 schemas,
taken after starting every service once so tables exist, with the 12
seeded test accounts already in it (passwords are bcrypt hashes, not
plaintext - safe to commit). It's a convenience, not a requirement: the
same tables and accounts get created automatically the first time you
start the services anyway (see "Day 1 setup" and auth-service's
`DataSeeder`). Use the dump if you want the data in place *before*
starting any service, e.g. to inspect it directly in a MySQL client.

1. Start just the MySQL container (skip this if it's already running):
   ```
   docker-compose up -d mysql
   ```
2. Import - from a bash shell (Git Bash, WSL, macOS, Linux):
   ```
   docker exec -i dpdms-mysql mysql -u root -proot_pass < dpdms-dump.sql
   ```
   From PowerShell (`<` redirection doesn't work the same way):
   ```
   Get-Content dpdms-dump.sql | docker exec -i dpdms-mysql mysql -u root -proot_pass
   ```
3. Verify: `docker exec dpdms-mysql mysql -u root -proot_pass -e "SELECT username, role FROM auth_db.users;"`
   should list all 12 test accounts.

The dump is idempotent-ish but not a merge: re-importing re-creates the
same 9 databases and re-inserts the same seed rows. If you've since
submitted real incidents through the app and don't want to lose them,
don't re-import over a database you care about - take a fresh dump
instead (see below).

**Re-generating the dump** (e.g. before a demo, to capture more test
data): with every service running at least once against the database,
```
docker exec dpdms-mysql mysqldump -u root -proot_pass \
  --databases auth_db flood_db drought_db fire_db zoonotic_db mining_db report_db alert_db dashboard_db \
  --routines --triggers --single-transaction --comments \
  > dpdms-dump.sql
```

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
- Web pages (`/mining-accident-service/web/submit`,
  `/web/my-submissions`, `/web/queue`) - plain Thymeleaf pages whose JS
  calls the REST API with a token from localStorage (set by the login
  page)
- RBAC/scoping enforced in the service layer via `HazardScopeGuard`
  (from the shared `common` module) - never trust the gateway alone
- Audit log recording every state transition

**Try it end to end** (with the frontend running per "Running the
frontend" above):
1. Open `http://localhost:5173/login`, log in as `mining.recorder` /
   `Password123!`
2. Go to the Submit page, submit an incident
3. Log out, log in as `mining.supervisor` / `Password123!`
4. Go to the Queue page, approve/reject/request-corrections on it

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
7. Update the Thymeleaf templates' hazard-specific form fields and
   table columns to match.
8. In `pom.xml` (root): uncomment your service's module line.
9. In gateway's `application.yml`: your route already exists (all 5
   hazard routes were pre-wired on Day 1) - nothing to change there.
10. Seed test accounts already exist for every hazard in auth-service's
    `DataSeeder` - use `<yourhazard>.recorder` / `<yourhazard>.supervisor`,
    password `Password123!`.
