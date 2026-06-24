# Query JDBC Executor Parity

## Scope

- Continued migration from legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/SqlScriptFac.cs` `Result(...)`.
- Migrated the concrete paged query execution adapter that runs count/page SQL and maps the result into `QueryResult`.
- Corrected positional parameter payloads for combined paged SQL so filter placeholders in count and page statements each receive values.
- Kept report-parameter binding and report/query orchestration beyond direct paged query execution as later slices.

## Changes

- Added `PagedQuerySql` to expose separate count/page SQL and separate count/page argument arrays.
- Added `JdbcQueryExecutor` using `JdbcTemplate` to execute count and page queries and populate `QueryResult` page metadata and rows.
- Updated `QuerySqlBuilder.pagedSql(...)` to build from `PagedQuerySql` and duplicate filter arguments correctly for combined count/page SQL.
- Added `JdbcQueryExecutorTest` with a recording `JdbcTemplate` to verify count/page SQL execution, argument ordering, and `QueryResult` mapping.
- Updated `docs/migration/foolframe-parity.md` to mark direct paged query execution as migrated.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=JdbcQueryExecutorTest -DfailIfNoTests=false test`
  - Result: test compile failed because `JdbcQueryExecutor` did not exist.
- Red for positional parameter correction:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QuerySqlBuilderTest -DfailIfNoTests=false test`
  - Result: test suite could not compile while `JdbcQueryExecutor` was still absent; the updated `QuerySqlBuilderTest` also locked the corrected combined SQL parameter contract.
- Green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=JdbcQueryExecutorTest,QuerySqlBuilderTest -DfailIfNoTests=false test`
  - Result: `JdbcQueryExecutorTest` and `QuerySqlBuilderTest`: 7 tests, 0 failures, 0 errors.

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: `SelectedTablesTest`, `CompareFilterTest`, `QueryLookupCollectionTest`, `JdbcQueryExecutorTest`, `QuerySqlBuilderTest`, `QueryInstanceMigrationTest`, `SelectTypeCatalogTest`, `CompareOpCatalogTest`, and `SelectedColumnCollectionTest`: 27 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 27.859 s.
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

- Report-parameter binding remains tied to later bool-expression/report parameter integration.
- Query-to-view integration can now call the direct paged executor, but richer view/query orchestration remains a later migration slice.
