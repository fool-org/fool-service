# Query Add-Table Result Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/AddQueryTable.cs` and the surrounding `SelectedTables.cs` add-table behavior.
- Migrated the `Success` / `NoRelation` / `Exists` result contract as a non-throwing Java surface around the existing selected-table join-add logic.
- Preserved the existing exception-throwing `SelectedTables.add(...)` API for current callers.

## Changes

- Added `AddQueryTable` enum with legacy values `Success`, `NoRelation`, and `Exists`.
- Added `SelectedTables.tryAdd(...)`, which returns `Exists` when the exact selected table is already present, `Success` when the existing join-add operation succeeds, and `NoRelation` when the legacy add path rejects the requested relation.
- Extended `SelectedTablesTest` to cover the non-throwing result contract and verify failed attempts do not mutate selected tables or joins.
- Updated `docs/migration/foolframe-parity.md` to record the migrated add-table result contract and bump `fool-query` to 41 Java main files.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=SelectedTablesTest -DfailIfNoTests=false test`
  - Result: test compile failed because `AddQueryTable` and `SelectedTables.tryAdd(...)` did not exist.
- Green:
  - Same command.
  - Result: `SelectedTablesTest`: 4 tests, 0 failures, 0 errors; `fool-query` compiled 41 main source files.

## Verification

- Focused selected-table verification:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=SelectedTablesTest -DfailIfNoTests=false test`
  - Result: `SelectedTablesTest`: 4 tests, 0 failures, 0 errors; `fool-query` compiled 41 main source files.
- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryContextTest,ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: 35 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - `docker run --rm --network fool-service_default -e SPRING_DATASOURCE_URL='jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 23.888 s.
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

- Saved-query/report surfaces remain a later migration slice.
- Richer query-to-view integration remains separate from this selected-table result-contract migration.
