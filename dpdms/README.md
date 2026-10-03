# DPDMS - Rushinga Provincial Disaster Monitoring and Management System

A microservice system for recording, approving and alerting on disaster
incidents (flood, drought, fire, zoonotic disease, mining accident)
across Rushinga's wards and provinces.

## Stack

- Java 17, Spring Boot 3.3.4, Spring Cloud 2023.0.3
- MySQL 8 (one schema per service)
- RabbitMQ (async alert dispatch)
- React + Vite frontend (`frontend/`)
- Eureka (service discovery) + Spring Cloud Gateway (single entry point)

## Starting the system

Prerequisites: Java 17, Maven, Node.js, Docker Desktop.

1. Start infrastructure:
   ```
   docker-compose up -d
   ```
   Starts MySQL (port 3306, creates all 9 schemas automatically via
   `mysql-init/`) and RabbitMQ (port 5672, management UI at
   http://localhost:15672, login guest/guest).
2. Start `discovery-service` (Eureka registry), then `gateway` - in
   that order, each in its own terminal:
   ```
   cd discovery-service && mvn spring-boot:run
   ```
   ```
   cd gateway && mvn spring-boot:run
   ```
   Confirm both are up at http://localhost:8761 (gateway should show as
   UP after ~10-20 seconds).
3. Start the remaining services (order between them doesn't matter -
   Eureka handles discovery): `auth-service`, `mining-accident-service`,
   `flood-service`, `drought-service`, `fire-service`,
   `zoonotic-disease-service`, `dashboard-service`, `report-service`,
   `alert-service`. Same pattern: `cd <service> && mvn spring-boot:run`.
4. Start the frontend:
   ```
   cd frontend
   npm install
   npm run dev
   ```
   Opens at http://localhost:5173. Vite proxies every `/xxx-service/**`
   request through to the gateway, so steps 1-3 must be running first.

**Logging in:** test accounts are seeded automatically the first time
`auth-service` starts. Password for all of them is `Password123!`:

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

**Environment variables:** every service falls back to a safe dev-only
default if an env var isn't set, so the steps above work with zero
configuration. See `.env.example` at the repo root for what each one
does (email/WhatsApp alert credentials, JWT secret). The one hard
constraint: `JWT_SECRET` must be set identically on `auth-service` and
`gateway` if you override it, or tokens issued by `auth-service` will
be rejected at the gateway.

### Database

MySQL's data lives in `mysql-data/` at the project root (bind-mounted
by `docker-compose.yml`) - it's created fresh the first time you run
`docker-compose up -d` and persists across restarts. To start over:
`docker-compose down -v`.

`dpdms-dump.sql` (repo root) is an optional pre-populated snapshot of
all 9 schemas, including the 12 seeded test accounts. To load it instead
of letting the services create everything themselves:
```
docker-compose up -d mysql
docker exec -i dpdms-mysql mysql -u root -proot_pass < dpdms-dump.sql
```
(PowerShell: `Get-Content dpdms-dump.sql | docker exec -i dpdms-mysql mysql -u root -proot_pass`)

## Hazard-level scoping

Every request carries a JWT from `auth-service` (issued at login, with
the user's role, hazard and ward baked in). The gateway's
`JwtAuthenticationFilter` validates that token and forwards the claims
as `X-User-Id`, `X-User-Role`, `X-User-Hazard` and `X-User-Ward`
headers to whichever service it's routing to - a service never sees a
raw token, only these pre-validated headers.

Each service resolves those headers into a `RequestContext` and passes
it to `HazardScopeGuard` (shared by every hazard service, from the
`common` module) before any read or write. The rules it enforces:

- **WARD_RECORDER** - may create/view/edit only their own (ward, hazard)
  records, and only their own submissions.
- **PROVINCIAL_SUPERVISOR** - may approve/reject/request-corrections
  only for their own hazard, province-wide (no ward restriction).
- **NATIONAL_VIEWER** - read-only, and only `APPROVED` records, across
  any hazard.
- **PROVINCIAL_ADMIN** - read-only, but may see pending records too
  (not just approved) - the brief gives them visibility, not approval
  power.

This check happens in each service's own service layer, never only at
the gateway - a service never trusts the gateway's routing alone to
enforce access control.

## Approval workflow

A `WARD_RECORDER` submits an incident, which starts as `PENDING`. From
there, the hazard's `PROVINCIAL_SUPERVISOR` can:

- **Approve** - status becomes `APPROVED`, and the service publishes an
  "incident approved" event for alert-service to pick up (see below).
- **Reject** - status becomes `REJECTED`, with the supervisor's notes.
- **Request corrections** - status becomes `CORRECTIONS_REQUESTED`,
  with notes explaining what's needed; the recorder can edit and
  resubmit, returning it to `PENDING`.

Every transition is written to that hazard's audit log (who, when,
what action, what notes) - this is a separate table from the incident
itself, so the full history survives even after the incident's current
status changes again.

## How alerts are dispatched

When a supervisor approves an incident, the hazard service publishes
an `IncidentApprovedEvent` to RabbitMQ (topic exchange `dpdms.incidents`,
routing key `incident.approved.<hazard>`, e.g. `incident.approved.flood`).
This is fire-and-forget: if RabbitMQ is unreachable, the approval still
saves, a warning is logged, and nothing blocks the recorder/supervisor.

`alert-service` consumes every `incident.approved.#` message and decides
whether to actually notify anyone:

1. Each hazard service flags whether its own hazard-specific criteria
   are met (e.g. mining: any fatalities or trapped/injured).
2. As a safety net, any incident at or above a configured severity
   (`dpdms.alerts.always-alert-severity`, default `CRITICAL`) always
   alerts, regardless of that flag.
3. If neither applies, the incident is still recorded in `alert-service`
   with status `SUPPRESSED` - visible for audit, but nobody is notified.

When an alert does fire, it's sent through every *enabled* channel to
every configured recipient: email (SMTP), and/or WhatsApp (either
Meta's WhatsApp Business Cloud API or Green API, an unofficial QR-paired
gateway - at most one of these two should be enabled at a time). If no
channel is enabled, the alert is written to the application log
instead. Every delivery attempt (success or failure, per channel, per
recipient) is recorded individually, and duplicate events for the same
incident are detected and ignored.

## Frontend

React + Vite, in `frontend/`. The project originally used server-rendered
Thymeleaf pages; those were replaced with this SPA. Reasoning: the SPA
talks to every backend service through the gateway using relative paths
(e.g. `/flood-service/api/incidents`), and in dev, Vite proxies each of
those prefixes straight through to the gateway - so the browser sees the
app and the API as the same origin. That gives the same "no CORS
configuration needed anywhere in the backend" property the old
Thymeleaf pages got for free by being server-rendered through the
gateway, while allowing a modern, componentized UI (role-based routing,
shared map/table/form components across all five hazards) instead of
duplicating server-rendered templates per hazard.
