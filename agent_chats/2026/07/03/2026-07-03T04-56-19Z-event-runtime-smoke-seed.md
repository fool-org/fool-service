# Event Runtime Smoke Seed

## Prompt

- Continue the active migration goal: Docker environment, FoolFrame parity,
  Vue frontend, and timely atomic commits.

## Scope

- Closed a real event runtime smoke gap:
  - `../FoolFrame` uses `SqlCon.ToString()` / `Data Source=...;Initial Catalog=...`
    SQL Server-style connection strings.
  - `fool-event` scoped runtime connection parsing did not accept that legacy
    shape.
  - Docker app/store seed data needed explicit MySQL JDBC URLs for the local
    MySQL smoke environment.
  - Docker had event/message tables but no running event definition seed.

## Changed Files

- `fool-event/src/main/java/org/fool/framework/event/DriverManagerEventJdbcTemplateFactory.java`
- `fool-event/src/test/java/org/fool/framework/event/EventMigrationTest.java`
- `docker/mysql/init/002-app-manage.sql`
- `docker/mysql/init/004-event.sql`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T04-56-19Z-event-runtime-smoke-seed.md`

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest#driverManagerEventJdbcTemplateFactoryParsesLegacySqlConStrings -DfailIfNoTests=false test`
  failed with `Event database JDBC url is required.`
- GREEN:
  same focused command passed after adding legacy connection parsing.
- Event module:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`
  passed with 43 tests.
- Running DB seed patch:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/002-app-manage.sql`
  passed.
- Running DB seed patch:
  `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/004-event.sql`
  passed.
- Backend image:
  `docker compose build backend` passed.
- Backend runtime:
  `docker compose up -d backend` passed.
- Backend smoke:
  `curl -sS http://localhost:8080/test` returned the seeded order rows.
- Event runtime smoke:
  `docker compose run -d --name fool-event-smoke -e FOOL_EVENT_SCHEDULER_ENABLED=true backend`
  then a MySQL query showed:
  `SW_EVT_DEF=1`, `SW_EVT_EVENT=1`, `SW_SYS_MSG=1`, event object `1001`,
  and admin message `MSG_USERID=admin`.
- Cleanup:
  `docker stop fool-event-smoke`
  and `docker rm fool-event-smoke` passed.
- Compose state:
  `docker compose ps` showed backend/frontend/mysql/redis running, with MySQL
  and Redis healthy.

## Skipped

- Did not enable the event scheduler in default `docker-compose.yml`; legacy
  `Global.asax.cs` had the event service startup commented out, so the smoke
  uses a one-off scheduler-enabled container.
- Did not add a new event REST endpoint; existing `EventMakeService` scheduler
  path is enough to prove the migrated runtime.

## Risks

- Event runtime still swallows a cycle-level exception like the legacy service,
  so deeper event failures require DB/log evidence rather than an API response.
- Remaining event parity still includes fuller dynamic object-query behavior.
