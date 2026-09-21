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
- Thymeleaf (server-rendered front end)
- Eureka (service discovery) + Spring Cloud Gateway (single entry point)
- Maven, single repo, multi-module

We chose Thymeleaf over a separate SPA framework to avoid adding a second
build pipeline and CORS configuration on top of nine backend services
within a 9-day project timeline.

## Project layout

```
dpdms/
  pom.xml                 <- parent, lists all modules
  discovery-service/      <- Eureka registry (port 8761)
  gateway/                <- API gateway, single entry point (port 8080)
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
