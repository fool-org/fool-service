# Query Instance Container Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/QueryInstance.cs`, `QueryParameter.cs`, `ReportParameter.cs`, `CompareCol.cs`, and `QueryResult.cs`.
- Migrated the query instance container layer, query/report parameter DTOs, compare-column wrapper, and query-result pagination metadata.
- Kept legacy `SqlScriptFac` SQL projection generation and database execution as later slices.

## Changes

- Added `CompareCol` with `QueryColumn` and selected table alias state.
- Added `QueryParameter` with legacy parameter name, compare-column, and value state.
- Added `ReportParameter` with name, expression, raw value, and formatted value state.
- Added `QueryInstance` with default selected-column, query-parameter, and report-parameter collections.
- Added `QueryResult` with page size, current page, total records, total pages, and current row data.
- Updated `docs/migration/foolframe-parity.md` to mark query instance parameter/result containers as migrated and narrow remaining `SWDQ01-Soway.Query` work.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryInstanceMigrationTest -DfailIfNoTests=false test`
  - Result: test compile failed because `QueryInstance`, `CompareCol`, `QueryParameter`, `ReportParameter`, and `QueryResult` did not exist.
- Green:
  - Same command.
  - Result: `QueryInstanceMigrationTest`: 3 tests, 0 failures, 0 errors.

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: `SelectedTablesTest`, `CompareFilterTest`, `QueryLookupCollectionTest`, `QueryInstanceMigrationTest`, `SelectTypeCatalogTest`, `CompareOpCatalogTest`, and `SelectedColumnCollectionTest`: 20 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 40.494 s.
- Backend compose image/runtime:
  - `docker compose build -q backend && docker compose up -d backend`: backend image rebuilt and container restarted.
  - `docker compose ps`: backend, frontend, mysql, and redis running; MySQL and Redis healthy.
- Runtime smoke after backend restart:
  - `GET http://localhost:8080/test`: response captured, 132 bytes.
  - `GET http://localhost:8081/`: response captured, 399 bytes.
  - `POST http://localhost:8080/api/v1/data/query-list` with `OrderList` `orderId` filter values `1001,1002`: response captured, 296 bytes.

## Remaining

- `SqlScriptFac` selected-column SQL projection, join SQL, grouping, row-number paging SQL, and result execution remain separate migration slices.
- `QueryInsFac.RefreshQueryInsReportParam` currently only triggers bool-expression SQL materialization in legacy and remains tied to later bool-expression/report parameter integration.
