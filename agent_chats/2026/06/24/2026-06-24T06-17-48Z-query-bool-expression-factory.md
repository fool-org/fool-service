# Query Bool Expression Factory

## Prompt

- Continue the active goal: bring the environment up with Docker, complete migration against `../FoolFrame`, use Vue for the frontend, and keep atomic commits timely.

## Scope

- Compared `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.BoolExp/BoolExpresstionFacotry.cs`.
- Added the Java-side bool-expression wrapper/factory surface for create/add orchestration.
- Reused existing `SimpleBoolExpression` and `CompositeFilter` SQL generation instead of introducing a parallel SQL builder.

## Changes

- Added `BoolExpression`, mirroring the legacy wrapper that owns a `QueryInstance` and expression node.
- Added `BoolExpressionFactory` with:
  - `createBoolExpression(...)` for factory-owned `SimpleBoolExpression` creation.
  - `addBoolExpression(first, op, second)` for in-place AND/OR composition.
  - `addBoolExpression(first, op, col, compareOp, value, showValue, paramName)` for legacy compare-column append behavior using the first expression owner.
- Added `BoolExpressionFactoryTest` covering owner selection, in-place add behavior, SQL text, ordered arguments, and report-parameter indexes.
- Left the mixed `docs/migration/foolframe-parity.md` status update unstaged for a separate status commit.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=BoolExpressionFactoryTest -DfailIfNoTests=false test`
  - Result: failed at test compilation because `BoolExpressionFactory` and `BoolExpression` were missing.
- Green:
  - Same command.
  - Result: `BoolExpressionFactoryTest`: 3 tests, 0 failures, 0 errors.

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=BoolExpressionFactoryTest,SimpleBoolExpressionTest,QueryFactoryTest,QueryInsFacTest,QueryReportTest,QueryContextTest,ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: 46 tests, 0 failures, 0 errors.

## Runtime Evidence

- Not rerun for this slice. This is a `fool-query` unit-level migration surface and does not change the running Compose API or Vue frontend.

## Risks

- Java `BoolOp` renders enum names (`AND` / `OR`) while legacy C# `BoolOp.DBName` included spacing and mixed case. The SQL semantics and parameter ordering are preserved through the existing Java filter stack.
- This does not complete saved-query/report execution or richer query-to-view integration.

## Follow-ups

- Continue Query saved-query/report execution parity.
- Commit this Query extension atomically with the rest of the validated Query migration once `.git` writes are available.
