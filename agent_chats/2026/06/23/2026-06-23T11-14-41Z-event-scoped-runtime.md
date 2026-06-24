# Event Application Catalog and Scoped Runtime Migration

## Scope

- Compared legacy event scheduler:
  - `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventMakeService.cs`
  - `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventDefFactory.cs`
  - `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventCheckFactory.cs`
  - `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventFactory.cs`
- Compared legacy app metadata models:
  - `../FoolFrame/src/Server/SCPB07-Soway.AppManage/App/Application.cs`
  - `../FoolFrame/src/Server/SCPB07-Soway.AppManage/App/StoreDataBase.cs`
  - `../FoolFrame/src/Server/SCPB05-Soway.Model/AssemblyModelFactory.cs`
- Migrated concrete app catalog loading and scoped event runtime wiring.

## Legacy Mapping

- `EventMakeService` reads applications from `SW_APPLICATION`.
- Each application uses `Application.SysCon` (`SW_APP_CON`) as the app system connection.
- Each application database comes from `Application.DataBase` / `StoreDataBase.Apps`, a reciprocal many-to-many relation:
  - relation table: `SW_APPLICATION_SW_STOREDB`
  - application relation column: `SW_APPLICATION_ID`
  - store DB relation column: `SW_STOREDB_ID`
  - store DB connection column: `SW_STORE_CON`
- Legacy runtime reads event definitions and auth recipients from the app system connection, then checks objects and writes events/messages against each store database connection.

## Changes

- Added `JdbcEventApplicationCatalog`:
  - reads `SW_APPLICATION.SW_APP_APPLICATIONID`
  - reads `SW_APPLICATION.SW_APP_CON`
  - loads database connections through `SW_APPLICATION_SW_STOREDB` and `SW_STOREDB.SW_STORE_CON`
- Added `EventJdbcTemplateFactory` and `DriverManagerEventJdbcTemplateFactory`.
- Added `JdbcScopedEventRuntime`:
  - builds system-scoped event definition and recipient repositories from the app system connection
  - builds object query, event record repository, and message repository from each database connection
- Added Docker seed schema for `SW_APPLICATION_SW_STOREDB`.
- Updated README and migration parity docs:
  - `fool-event` Java main files: `40`.
  - remaining event gaps now focus on background startup/scheduling configuration and fuller model metadata based object-query parity.

## TDD Evidence

- Red: `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -Dtest=EventMigrationTest test`
  - Failed compiling because `JdbcEventApplicationCatalog` and `JdbcScopedEventRuntime` did not exist.
- Green: same command passed after adding catalog, JDBC template factory, and scoped runtime.
  - `Tests run: 27, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Passed full Maven reactor package.
  - Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/002-app-manage.sql`
  - Applied updated app/store-db relation schema to the current MySQL volume.
- `docker compose up -d --build backend`
  - Passed backend image build and container restart.
- `docker compose ps --all`
  - backend and frontend are running; MySQL and Redis are running and healthy.
- `docker compose logs --tail=120 backend`
  - Spring Boot application started successfully with the `docker` profile.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - HTTP 200, body `[]`.
- `curl -I --max-time 10 http://localhost:8081/`
  - HTTP 200.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM SW_APPLICATION_SW_STOREDB;"`
  - Confirmed `SW_APPLICATION_ID` and `SW_STOREDB_ID`.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Remaining Event Gaps

- Background startup/scheduling configuration still needs an explicit Spring lifecycle decision.
- Object query parity still uses direct table-name SQL instead of full legacy model metadata expansion.
