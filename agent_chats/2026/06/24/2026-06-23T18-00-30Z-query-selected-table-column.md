# Query Selected Table and Column Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/SelectedCol.cs`, `SelectedColCollection.cs`, `SelectedTable.cs`, `SelectedTables.cs`, `Join.cs`, `JoinTables.cs`, `IQueryColumns.cs`, `IQueryTable.cs`, and `IQueryFactory.cs`.
- Migrated the selected output-column/table state models and the first selected table join-add behavior.
- Kept `SelectedTypeFac`, table/column lookup collections, and query/report instance orchestration as later slices.

## Changes

- Added query table/column state DTOs: `QueryTable`, `QueryColumn`, `ColStateValue`, `SelectType`, `SelectedTable`, and `SelectedColumn`.
- Added `SelectedColumnCollection` with legacy duplicate `SelectedName` rejection and insertion-order `selectedIndex` assignment.
- Added join primitives: `JoinCondition`, `JoinTable`, `JoinQueryType`, and functional `QueryFactory`.
- Added `SelectedTables` with legacy checks for selected `from` table, missing join condition, and direction-flipped join columns.
- Updated `docs/migration/foolframe-parity.md` for the new `fool-query` surface and remaining work.

## TDD

- Red, selected columns:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=SelectedColumnCollectionTest -DfailIfNoTests=false test`
  - Result: test compile failed because `QueryColumn` and selected-column classes were missing.
- Green, selected columns:
  - Same command.
  - Result: `SelectedColumnCollectionTest`: 2 tests, 0 failures, 0 errors.
- Red, selected tables:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=SelectedTablesTest -DfailIfNoTests=false test`
  - Result: test compile failed because `JoinTable` and selected-table join classes were missing.
- Green, selected tables:
  - Same command.
  - Result: `SelectedTablesTest`: 3 tests, 0 failures, 0 errors.

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: `SelectedTablesTest`, `CompareFilterTest`, `CompareOpCatalogTest`, and `SelectedColumnCollectionTest`: 11 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 34.542 s.
- Backend compose image/runtime:
  - `docker compose build -q backend && docker compose up -d backend`: backend image rebuilt and container restarted.
  - `docker compose ps`: backend, frontend, mysql, and redis running; MySQL and Redis healthy.
- Runtime smoke after backend restart:
  - `GET http://localhost:8080/test`: response captured, 132 bytes.
  - `GET http://localhost:8081/`: response captured, 399 bytes.
  - `POST http://localhost:8080/api/v1/data/query-list` with `OrderList` `orderId` filter values `1001,1002`: response captured, 296 bytes.

## Remaining

- `SelectedTypeFac` persisted select-type catalog loading is not migrated yet.
- `TableCollection` and `ColCollection` name-based lookup behavior is not migrated yet.
- Query/report instance orchestration and SQL projection generation remain separate migration slices.
