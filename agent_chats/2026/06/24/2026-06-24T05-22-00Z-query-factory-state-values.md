# QueryFactory Dictionary Parity

## Prompt

- Continue the active goal: bring the environment up with Docker, complete migration against `../FoolFrame`, and use Vue for the frontend.

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.Entity/IQueryFactory.cs`.
- Migrated the `GetTables` / `GetTable` / `GetColumns` dictionary surface.
- Migrated the `GetStateValues` / `GetStateStr` dictionary surface used to map a displayed state value to the database state value.

## Changes

- Added default `QueryFactory.getTables()`, `getTable(String)`, and `getColumns(QueryTable)` methods.
- Added default `QueryFactory.getStateValues(QueryColumn)` and `QueryFactory.getStateStr(QueryColumn, String)` methods.
- Kept `QueryFactory` as a functional interface so existing lambda-based join tests and `QueryContext` construction continue to work.
- Added `QueryFactoryTest` for table lookup by display/database name, default empty column lookup, display-state to database-state mapping, and unknown-value fallback.
- Updated `docs/migration/foolframe-parity.md` to record `QueryFactory` table/column/state-value dictionary mapping.

## TDD

- State-value red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryFactoryTest -DfailIfNoTests=false test`
  - Result: test compilation failed because `QueryFactory.getStateValues` / `getStateStr` did not exist.
- State-value green:
  - Same command.
  - Result: `QueryFactoryTest`: 1 test, 0 failures, 0 errors.
- Table/column dictionary red:
  - Same command.
  - Result: test compilation failed because `QueryFactory.getTables` / `getTable` / `getColumns` did not exist.
- Table/column dictionary green:
  - Same command.
  - Result: `QueryFactoryTest`: 2 tests, 0 failures, 0 errors.

## Verification

- Focused QueryFactory verification:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryFactoryTest -DfailIfNoTests=false test`
  - Result: `QueryFactoryTest`: 2 tests, 0 failures, 0 errors.
- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryFactoryTest,QueryInsFacTest,QueryReportTest,QueryContextTest,ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: 40 tests, 0 failures, 0 errors.

## Runtime Evidence

- Full backend package on the Compose network:
  - `docker run --rm --network fool-service_default -e SPRING_DATASOURCE_URL='jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 23.636 s.
- Backend Compose image/runtime:
  - `docker compose build -q backend && docker compose up -d backend`: backend image rebuilt and container restarted.
  - `docker compose ps`: backend, frontend, mysql, and redis running; MySQL and Redis healthy.
- Runtime smoke after backend restart:
  - `GET http://localhost:8080/test`: response captured, 132 bytes.
  - `GET http://localhost:8081/`: Vue frontend response captured, 399 bytes.
  - `POST http://localhost:8080/api/v1/data/query-list` with `OrderList` `orderId` filter values `1001,1002`: response captured, 296 bytes.
- Repository hygiene:
  - `python scripts/check_repo_harness.py`: passed.
  - `git diff --check`: passed.

## Risks

- The new methods are default methods; callers that have a real dictionary should override `getTables`, `getColumns`, and `getStateValues`.
- Unknown display values currently fall back to the input value to preserve existing behavior for callers without dictionary data.

## Follow-ups

- Continue saved-query/report execution and richer query-to-view integration migration.
