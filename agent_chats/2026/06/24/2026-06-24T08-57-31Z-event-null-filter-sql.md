# Event null-filter SQL parity

## Prompt

- Continue the active migration goal after Docker permissions were granted.

## Scope

- Compared `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventCheckFactory.cs` and `SqlHelper.cs`.
- Preserved the legacy `String.Format("SELECT * FROM {0} WHERE {1}", table, def.Filter)` behavior where a null filter renders as empty text, not the literal string `null`.

## Changes

- Added `EventMigrationTest.eventSqlHelperRendersLegacyNullFilterAsEmptyText`.
- Updated `EventSqlHelper.buildQuerySql` to convert a null filter to `""` before formatting.
- Updated `docs/migration/foolframe-parity.md` to record the migrated null-filter command rendering behavior.

## Validation

- Docker runtime retry:
  - `docker compose up -d --build`
  - Result: backend and frontend images built; MySQL and Redis healthy; backend restarted.
- Runtime smoke:
  - `docker compose ps`
  - `curl -sf http://localhost:8080/test`
  - `curl -sfI http://localhost:8081/`
  - Result: backend returned seeded order JSON; frontend returned `HTTP/1.1 200 OK`.
- Red test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest#eventSqlHelperRendersLegacyNullFilterAsEmptyText -DfailIfNoTests=false test`
  - Result: failed because Java rendered `WHERE null` instead of legacy `WHERE `.
- Green test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest#eventSqlHelperRendersLegacyNullFilterAsEmptyText -DfailIfNoTests=false test`
  - Result: passed.
- Focused event migration suite:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`
  - Result: passed.

## Runtime Evidence

- Compose services after retry:
  - `fool-service-backend-1`: Up on `0.0.0.0:8080->8080/tcp`
  - `fool-service-frontend-1`: Up on `0.0.0.0:8081->80/tcp`
  - `fool-service-mysql-1`: healthy on `127.0.0.1:3307->3306/tcp`
  - `fool-service-redis-1`: healthy on `127.0.0.1:6380->6379/tcp`

## Risks

- Full reactor `mvn test`, frontend `npm test`, and frontend `npm run build` were not rerun for this narrow Event SQL helper slice.

## Follow-ups

- Continue remaining `SCPB09-SOWAY.EVENT` dynamic object-query parity beyond the already covered null-model, table/id-column resolution, key-column validation, row-value capture, and filter command rendering behavior.
