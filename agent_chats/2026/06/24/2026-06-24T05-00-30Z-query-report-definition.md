# Query Report Definition Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/QueryReport.cs`.
- Migrated the query-report definition contract for SQL text, output columns, query parameters, report name, and report number.
- Kept saved-query/report execution and query-to-view integration out of this slice.

## Changes

- Added `QueryReport` interface with legacy-shaped accessors.
- Added `QueryReportDefinition`, a minimal live-list implementation for report SQL, output columns, query parameters, report name, and report number.
- Added `QueryReportTest` to verify the Java implementation can be consumed through the legacy contract surface.
- Updated `docs/migration/foolframe-parity.md` to record the query report definition contract and bump `fool-query` to 43 Java main files.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryReportTest -DfailIfNoTests=false test`
  - Result: test compile failed because `QueryReport` and `QueryReportDefinition` did not exist.
- Green:
  - Same command.
  - Result: `QueryReportTest`: 1 test, 0 failures, 0 errors; `fool-query` compiled 43 main source files.

## Verification

- Focused query report verification:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryReportTest -DfailIfNoTests=false test`
  - Result: `QueryReportTest`: 1 test, 0 failures, 0 errors; `fool-query` compiled 43 main source files.
- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryReportTest,QueryContextTest,ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: 36 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - `docker run --rm --network fool-service_default -e SPRING_DATASOURCE_URL='jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 24.031 s.
- Backend compose image/runtime:
  - `docker compose build -q backend && docker compose up -d backend`: backend image rebuilt and container restarted.
  - `docker compose ps`: backend, frontend, mysql, and redis running; MySQL and Redis healthy.
- Runtime smoke after backend restart:
  - `GET http://localhost:8080/test`: response captured, 132 bytes.
  - `GET http://localhost:8081/`: response captured, 399 bytes.
  - `POST http://localhost:8080/api/v1/data/query-list` with `OrderList` `orderId` filter values `1001,1002`: response captured, 296 bytes.
- Repository hygiene:
  - `python scripts/check_repo_harness.py`: passed.
  - `git diff --check`: passed.

## Remaining

- Saved-query/report execution surfaces remain a later migration slice.
- Richer query-to-view integration remains separate from this definition-contract migration.
