# Event Matched Row Values Migration

## Scope

- Compared legacy `../FoolFrame/src/Server/SCPB09-SOWAY.EVENT/EventCheckFactory.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlServer/dbContext.cs`.
- Compared legacy `../FoolFrame/src/Server/SCPB05-Soway.Model/SqlServer/SqlDataLoader.cs`.
- Migrated event matched objects from ID-only records to ID plus captured row values.

## Legacy Mapping

- Legacy `EventCheckFactory.GetObjs` executes `SqlHelper.GetQueryCommand(model, def)` and passes the returned table rows through `dbContext.GetBySqlCommand`.
- Legacy `dbContext.GetBySqlCommand` requires the model key column, creates a proxy from the key, and calls `SqlDataLoader.LoadSqlData(proxy, row, ...)`.
- Legacy `SqlDataLoader.LoadSqlData` copies available row columns into the dynamic object by model property metadata.
- Java now keeps the object ID while also retaining the JDBC result row values on `EventMatchedObject`.

## Changes

- Extended `EventMatchedObject` to hold immutable `values`.
- Kept the one-argument `EventMatchedObject(String objectId)` constructor for existing callers.
- Updated `JdbcEventObjectQuery` to copy `ResultSet` columns via metadata into matched-object values.
- Updated event migration tests to assert that object query returns object ID and row values.
- Updated README and migration parity docs.

## TDD Evidence

- Red:
  - `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-event -am -Dtest=EventMigrationTest -DfailIfNoTests=false test`
  - Failed compiling because `EventMatchedObject.values()` did not exist.
- Green:
  - same command passed after adding matched row value capture.
  - `Tests run: 34, Failures: 0, Errors: 0, Skipped: 0`

## Verification

- `docker run --rm -v "$HOME/.m2":/root/.m2 -v "$PWD":/workspace -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package`
  - Full 15-module reactor passed.
  - `fool-event` compiled 45 main source files.
- `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- `git diff --check`
  - Passed with no whitespace errors.
- `docker compose up -d --build backend`
  - Rebuilt backend image successfully.
  - Restarted `fool-service-backend-1` on Java 17.
- `docker compose ps --all`
  - `backend`, `frontend`, `mysql`, and `redis` are running.
- `curl --retry 5 --retry-connrefused --retry-delay 2 -i --max-time 10 http://localhost:8080/test`
  - HTTP 200, body `[]`.
- `curl -I --max-time 10 http://localhost:8081/`
  - HTTP 200 from nginx frontend.

## Remaining Event Gaps

- Matched objects now retain row values, but there is still no legacy property-name expansion or message/object format templating on top of those values.
- Auto system ID fallback remains the existing direct-column fallback until migrated model metadata exposes `AutoSysId`.
