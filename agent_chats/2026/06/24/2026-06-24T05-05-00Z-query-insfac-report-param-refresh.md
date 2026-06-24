# QueryInsFac Report Parameter Refresh Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/QueryInsFac.cs`.
- Migrated the report-parameter refresh path that materializes report parameters from a query instance's bool expression without executing a database query.
- Reused the existing Java `ReportCompareFilter` side effect so parameter names, expressions, values, and display values stay consistent with the SQL-builder path.

## Changes

- Added `QueryInsFac.refreshQueryInsReportParam(QueryInstance)`.
- Added `QueryInsFacTest` for composite filter parameter indexing and reuse of existing report parameter values.
- Updated `docs/migration/foolframe-parity.md` to record report-parameter refresh orchestration and bump `fool-query` to 44 Java main files.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryInsFacTest -DfailIfNoTests=false test`
  - Result: test compile failed because `QueryInsFac` did not exist.
- Green:
  - Same command.
  - Result: `QueryInsFacTest`: 2 tests, 0 failures, 0 errors; `fool-query` compiled 44 main source files.

## Verification

- Focused QueryInsFac verification:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryInsFacTest -DfailIfNoTests=false test`
  - Result: `QueryInsFacTest`: 2 tests, 0 failures, 0 errors; `fool-query` compiled 44 main source files.
- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryInsFacTest,QueryReportTest,QueryContextTest,ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: 38 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - `docker run --rm --network fool-service_default -e SPRING_DATASOURCE_URL='jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 24.063 s.
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
- Richer query-to-view integration remains separate from this report-parameter refresh migration.
