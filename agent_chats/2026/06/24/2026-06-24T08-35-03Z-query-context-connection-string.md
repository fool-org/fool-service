# QueryContext Connection String Parity

## Prompt

- Continue the `/goal`: migrate against `../FoolFrame`, keep Docker/Vue runtime usable, and commit atomically.

## Scope

- Compared `../FoolFrame/src/Server/SWDQ01-Soway.Query/QueryContext.cs`.
- Migrated the legacy `QueryConStr` constructor state into Java `QueryContext`.
- Added a connection-string constructor and getter.
- Verified `clear()` still only replaces the query instance and preserves the connection-string state, matching the legacy implementation.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QueryContext.java`
- `fool-query/src/test/java/org/fool/framework/query/QueryContextTest.java`
- `docs/migration/foolframe-parity.md`

## TDD Evidence

- Red test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=QueryContextTest#constructorKeepsLegacyQueryConnectionStringAcrossClear -DfailIfNoTests=false test`
  - Result: test compilation failed because `String` could not be passed as the second `QueryContext` argument and `getQueryConnectionString()` did not exist.
- Green focused test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=QueryContextTest#constructorKeepsLegacyQueryConnectionStringAcrossClear -DfailIfNoTests=false test`
  - Result: passed.
- QueryContext regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=QueryContextTest -DfailIfNoTests=false test`
  - Result: `QueryContextTest`: 8 tests, 0 failures, 0 errors.
- Query focused regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=QueryContextTest,BoolExpressionFactoryTest,SimpleBoolExpressionTest,QueryFactoryTest,QueryInsFacTest,QueryReportTest,ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: passed.

## Runtime / Skips

- No runtime or frontend behavior changed in this slice.
- Repository harness:
  - `python scripts/check_repo_harness.py`
  - Result: passed.
- Whitespace check:
  - `git diff --check`
  - Result: passed.
- Docker rebuild and Vue build were deferred to broader runtime validation.

## Follow-ups

- Continue saved-query/report execution and query-to-view integration parity.
- Continue the broader FoolFrame parity checklist in `docs/migration/foolframe-parity.md`.
