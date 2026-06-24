# Query Report Filter Parity

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.BoolExp/SimpleBoolExpression.cs`, `ComplexBoolExpression.cs`, and `QueryInsFac.cs`.
- Migrated the report-parameter reuse behavior for named simple compare expressions.
- Preserved Java/JDBC positional placeholders while retaining the legacy report-parameter metadata (`name`, `exp`, `value`, `fmtValue`) on `QueryInstance`.

## Changes

- Added `ReportCompareFilter` for legacy `SimpleBoolExpression.ParamName` behavior.
- Added indexed filter generation via `IQueryFilter.generateSql(int)` and `CompositeFilter.generateSql(int)` so report filters can record the legacy `@pN` expression position.
- Existing non-report filters continue to use the original `generateSql()` API.
- Updated `docs/migration/foolframe-parity.md` to mark report filter SQL generation and report-parameter reuse as migrated.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=ReportCompareFilterTest -DfailIfNoTests=false test`
  - Result: test compile failed because `ReportCompareFilter` did not exist.
- Green:
  - Same command.
  - Result: `ReportCompareFilterTest`: 3 tests, 0 failures, 0 errors.

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: `SelectedTablesTest`, `CompareFilterTest`, `ReportCompareFilterTest`, `QueryLookupCollectionTest`, `JdbcQueryExecutorTest`, `QuerySqlBuilderTest`, `QueryInstanceMigrationTest`, `SelectTypeCatalogTest`, `CompareOpCatalogTest`, and `SelectedColumnCollectionTest`: 30 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 25.089 s.
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

- Report/query orchestration beyond direct paged query execution remains a later migration slice.
- Richer query-to-view integration remains separate from this filter-layer migration.
