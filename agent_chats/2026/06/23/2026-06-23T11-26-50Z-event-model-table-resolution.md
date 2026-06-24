# Event Model Table Resolution Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/SqlHelper.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/Model.cs`.
- Migrated event object query from direct `EVTDEF_MODEL` table usage toward FoolFrame parity: resolve event model metadata first, then query the resolved data table.

## Legacy Mapping

- Legacy `EventCheckFactory.GetObjs` resolves `def.DefModel` through model metadata before querying.
- Legacy `SqlHelper.GetQueryCommand(model, def)` uses `model.DataTableName`.
- Java model metadata stores the migrated equivalent as `fool_sys_model.table_name`.

## Changes

- Added `EventModelTableResolver`.
- Added `JdbcEventModelTableResolver`:
  - queries `fool_sys_model`
  - resolves by string form of `id` or by `name`
  - returns `table_name`
  - falls back to the input model id if metadata is absent, preserving the previous direct-table behavior
- Updated `JdbcEventObjectQuery` to build object SQL from the resolved table name.
- Updated `JdbcScopedEventRuntime` to resolve model metadata from the scoped system JDBC template and query objects from the scoped database JDBC template.
- Added Docker MySQL init DDL for `fool_sys_model`.
- Updated README and migration parity docs.

## TDD Evidence

- Added focused tests before implementation:
  - `jdbcEventModelTableResolverLoadsMigratedModelTableName`
  - `jdbcEventModelTableResolverFallsBackToModelIdWhenMetadataIsMissing`
  - `jdbcEventObjectQueryUsesResolvedModelTableName`
  - `jdbcEventObjectQueryMarksResolverConstructorForSpringInjection`
- Local Maven could not execute the red run to test compilation because the shell default Java is 8 and this project targets 17.
- Containerized focused verification caught the remaining constructor migration gap:
  - `JdbcScopedEventRuntime` still called `new JdbcEventObjectQuery(databaseJdbcTemplate)`.
- Green focused run:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`
  - `Tests run: 34, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Passed full Maven reactor package.
  - Existing Maven warning remains: duplicate `spring-jdbc` dependency in `fool-dao/pom.xml`.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
  - Applied the new model metadata schema to the running MySQL volume.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash -e "SHOW COLUMNS FROM fool_sys_model;"`
  - Confirmed `id`, `name`, and `table_name` columns.
- `docker compose up -d --build backend`
  - Passed backend image build and container restart.
- `docker compose ps --all`
  - backend and frontend are running; MySQL and Redis are running and healthy.
- `docker compose logs --tail=160 backend`
  - Spring Boot application started successfully with the `docker` profile.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - HTTP 200, body `[]`.
- `curl -I --max-time 10 http://localhost:8081/`
  - HTTP 200.
- `python scripts/check_repo_harness.py`
  - Passed.
- `git diff --check`
  - Passed.

## Remaining Event Gaps

- Object query now resolves the model table name, but still does not expand full model property metadata or legacy object/message formatting.
