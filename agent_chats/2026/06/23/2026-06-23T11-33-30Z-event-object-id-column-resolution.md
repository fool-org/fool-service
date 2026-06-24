# Event Object ID Column Resolution Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventCheckFactory.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlServer/SqlHelper.cs`.
- Migrated event object-query metadata from table-name-only resolution to table-name plus object ID column resolution.

## Legacy Mapping

- Legacy `EventCheckFactory.GetObjs` resolves `DefModel`, queries by `model.DataTableName`, and returns dynamic objects.
- Legacy `SqlHelper.GetKeyCol(model)` uses `model.IdProperty.DBName`, or `SYSID` for auto system IDs.
- Legacy event and message creation store `(obj.ID ?? "").ToString()` in event/message object ID fields.
- Java now resolves the migrated equivalent from `fool_sys_model.id_property` to `fool_sys_model_property.column`.

## Changes

- Added `EventModelQueryMetadata(tableName, objectIdColumn)`.
- Extended `EventModelTableResolver` to return query metadata while keeping `resolveTableName()` for compatibility.
- Updated `JdbcEventModelTableResolver`:
  - joins `fool_sys_model_property`
  - reads `property.column AS object_id_column`
  - falls back to direct model ID and `ID` when metadata is absent
- Updated `JdbcEventObjectQuery` to read matched object IDs from the configured `objectIdColumn`.
- Extended Docker MySQL init DDL with `fool_sys_model_property`.
- Updated README and migration parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `EventModelQueryMetadata` and `JdbcEventModelTableResolver.resolve(String)` did not exist.
- Green:
  - same command passed after adding metadata resolution and object-query wiring.
  - `Tests run: 34, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Full 15-module reactor passed.
  - `fool-event` compiled 45 main source files.
- `docker compose exec -T mysql mysql -uroot -pPa88word car_wash < docker/mysql/init/005-model.sql`
  - Applied the updated model metadata schema to the running Docker MySQL instance.
  - Confirmed `fool_sys_model_property` columns and indexes with `SHOW COLUMNS`.
- `docker compose up -d --build backend`
  - Rebuilt backend image successfully.
  - Restarted `fool-service-backend-1` on Java 17.
- `docker compose ps --all`
  - `backend`, `frontend`, `mysql`, and `redis` are running.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - HTTP 200, body `[]`.
- `curl -I --max-time 10 http://localhost:8081/`
  - HTTP 200 from nginx frontend.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- `git diff --check`
  - Passed with no whitespace errors.

## Remaining Event Gaps

- Object query now resolves table and ID column metadata, but still does not expand selected property metadata or legacy object/message formatting.
- Auto system ID fallback remains the existing direct-column fallback until the migrated model metadata exposes `AutoSysId`.
