# Query BoolOp Token Parity

## Prompt

- Continue the `/goal`: migrate against `../FoolFrame`, keep Docker/Vue runtime usable, and commit atomically.

## Scope

- Compared `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.BoolExp/BoolOp.cs`.
- Migrated legacy `BoolOp` display and SQL token metadata:
  - `AND`: DBName ` And `, ShowName `并且`
  - `OR`: DBName ` OR `, ShowName `或者`
- Updated composite filter SQL generation to join child expressions using the legacy DBName token instead of enum `name()`.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/BoolOp.java`
- `fool-query/src/main/java/org/fool/framework/query/CompositeFilter.java`
- `fool-query/src/test/java/org/fool/framework/query/CompareFilterTest.java`
- `fool-query/src/test/java/org/fool/framework/query/BoolExpressionFactoryTest.java`
- `fool-query/src/test/java/org/fool/framework/query/ReportCompareFilterTest.java`
- `docs/migration/foolframe-parity.md`

## TDD Evidence

- Red test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=CompareFilterTest#compositeFilterKeepsLegacyExpressionParenthesesAndArgumentOrder,CompareFilterTest#boolOpKeepsLegacyDisplayAndSqlTokens -DfailIfNoTests=false test`
  - Result: test compilation failed because `BoolOp#getDbName()` and `BoolOp#getShowName()` did not exist.
- Green focused test:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=CompareFilterTest#compositeFilterKeepsLegacyExpressionParenthesesAndArgumentOrder,CompareFilterTest#boolOpKeepsLegacyDisplayAndSqlTokens -DfailIfNoTests=false test`
  - Result: passed.
- Query focused regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -Dtest=QueryContextTest,BoolExpressionFactoryTest,SimpleBoolExpressionTest,QueryFactoryTest,QueryInsFacTest,QueryReportTest,ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - First result after implementation failed because older tests still expected uppercase `AND`; final result after updating those assertions: passed.

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
