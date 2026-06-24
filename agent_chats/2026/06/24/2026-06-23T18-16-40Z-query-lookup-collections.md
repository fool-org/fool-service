# Query Lookup Collection Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/TableCollection.cs` and `ColCollection.cs`.
- Migrated table lookup by database name or display name.
- Migrated column lookup by database name, display name, `table_db.column_db`, and `table_show.column_show`.
- Preserved legacy behavior where unmatched expressions return `null`.

## Changes

- Added `QueryTableCollection` as the Java equivalent surface for legacy `TableCollection`.
- Added `QueryColumnCollection` as the Java equivalent surface for legacy `ColCollection`.
- Both collections keep legacy trim + case-insensitive matching.
- Updated `docs/migration/foolframe-parity.md` to mark table/column lookup collections as migrated and narrow the remaining `SWDQ01-Soway.Query` work.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryLookupCollectionTest -DfailIfNoTests=false test`
  - Result: test compile failed because `QueryTableCollection` and `QueryColumnCollection` did not exist.
- Green:
  - Same command.
  - Result: `QueryLookupCollectionTest`: 2 tests, 0 failures, 0 errors.

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: `SelectedTablesTest`, `CompareFilterTest`, `QueryLookupCollectionTest`, `SelectTypeCatalogTest`, `CompareOpCatalogTest`, and `SelectedColumnCollectionTest`: 17 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 32.968 s.
- Backend compose image/runtime:
  - `docker compose build -q backend && docker compose up -d backend`: backend image rebuilt and container restarted.
  - `docker compose ps`: backend, frontend, mysql, and redis running; MySQL and Redis healthy.
- Runtime smoke after backend restart:
  - `GET http://localhost:8080/test`: response captured, 132 bytes.
  - `GET http://localhost:8081/`: response captured, 399 bytes.
  - `POST http://localhost:8080/api/v1/data/query-list` with `OrderList` `orderId` filter values `1001,1002`: response captured, 296 bytes.

## Remaining

- Query/report instance orchestration and SQL projection generation remain separate migration slices.
