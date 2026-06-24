# QueryContext Nonpaged SQL Surface

## Prompt

- Continue the active goal: bring the environment up with Docker, complete migration against `../FoolFrame`, use Vue for the frontend, and keep atomic commits timely.

## Scope

- Compared `../FoolFrame/src/Server/SWDQ01-Soway.Query/QueryContext.cs`.
- Migrated the legacy `QueryContext.GetSql()` surface to the Java `QueryContext` as a thin wrapper around the existing `QuerySqlBuilder`.
- Preserved the old behavior where enum selected columns load state-value mappings before SQL generation.

## Changes

- Added `QueryContext#getSql()`.
- Added `QueryContext#getSql(String rowIndex)`.
- Added `QueryContextTest#getSqlReturnsLegacyNonPagedSelectAndLoadsEnumStateValues`.
- Left the mixed `docs/migration/foolframe-parity.md` status update unstaged for a separate status commit.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryContextTest#getSqlReturnsLegacyNonPagedSelectAndLoadsEnumStateValues -DfailIfNoTests=false test`
  - Result: failed at test compilation because `QueryContext#getSql(String)` was missing.
- Green:
  - Same command.
  - Result: `QueryContextTest`: 1 test, 0 failures, 0 errors.

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryContextTest,BoolExpressionFactoryTest,SimpleBoolExpressionTest,QueryFactoryTest,QueryInsFacTest,QueryReportTest,ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: 47 tests, 0 failures, 0 errors.
- Repo hygiene checks:
  - `python scripts/check_repo_harness.py`
  - Result: passed.
  - `git diff --check`
  - Result: passed.
  - `git diff --cached --check`
  - Result: passed.

## Runtime Evidence

- Not rerun for this slice. This is a unit-level `fool-query` API migration and does not change the running Compose API or Vue frontend.

## Risks

- This does not implement saved-query persistence; legacy `QueryContext.Save()` was `NotImplementedException`.
- This does not add query-to-view integration beyond the already migrated SQL builder and executor surfaces.
- Atomic commit is still blocked while this workspace cannot write `.git/index.lock`.

## Follow-ups

- Continue saved-query/report execution and query-to-view integration parity.
- Commit this QueryContext SQL surface atomically once `.git` writes are available.
