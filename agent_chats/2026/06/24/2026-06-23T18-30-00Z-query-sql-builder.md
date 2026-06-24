# Query SQL Builder Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/SqlScriptFac.cs`.
- Migrated selected table SQL generation, join condition SQL generation, selected column SQL generation, enum `CASE WHEN` projection generation, and base `SELECT distinct` SQL with `ROW_NUMBER` ordering and legacy `GROUP BY` behavior.
- Kept paged result wrapping, SQL command parameter binding, and concrete query/report execution as later slices.

## Changes

- Added `QuerySqlBuilder` as the Java equivalent surface for legacy `SqlScriptFac` SQL string generation.
- `tableSql` emits legacy selected-table aliases and join predicates using the normalized final SQL shape.
- `selectedColumnSql` preserves legacy expression alias formatting and enum value `CASE WHEN` rendering.
- `selectSql` emits the legacy distinct projection, selected-column ordering, row index expression, joined table SQL, optional filter SQL text, and legacy grouping rule.
- Updated `docs/migration/foolframe-parity.md` to mark the base query SQL builder as migrated and narrow remaining `SWDQ01-Soway.Query` work.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QuerySqlBuilderTest -DfailIfNoTests=false test`
  - Result: test compile failed because `QuerySqlBuilder` did not exist.
- Green:
  - Same command.
  - Result: `QuerySqlBuilderTest`: 4 tests, 0 failures, 0 errors.

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: `SelectedTablesTest`, `CompareFilterTest`, `QueryLookupCollectionTest`, `QuerySqlBuilderTest`, `QueryInstanceMigrationTest`, `SelectTypeCatalogTest`, `CompareOpCatalogTest`, and `SelectedColumnCollectionTest`: 24 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 45.239 s.
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

- `SqlScriptFac.Result` paging result wrapping and parameterized command execution remain separate migration slices.
- `QueryInsFac.RefreshQueryInsReportParam` remains tied to later bool-expression/report parameter integration.
