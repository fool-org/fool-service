# Query Simple Bool Expression

## Prompt

- Continue the active goal: bring the environment up with Docker, complete migration against `../FoolFrame`, and use Vue for the frontend.

## Scope

- Compared legacy `../FoolFrame/src/Server/SWDQ01-Soway.Query/Soway.Query.BoolExp/SimpleBoolExpression.cs`.
- Migrated the selected-table compare-column SQL behavior into Java `SimpleBoolExpression`.
- Covered the report-parameter creation path that assigns `@p{index}` when a named parameter is first materialized.

## Changes

- Added `SimpleBoolExpression` implementing the existing Java `IQueryFilter` surface through `SimpleFilter`.
- Generated legacy-style selected-table/column SQL such as `[Orders].[STATUS]= ?` from `CompareCol`.
- Added named report-parameter creation using the passed `parameterStartIndex`, preserving `Name`, `Exp`, `Value`, and `FmtValue`.
- Added `SimpleBoolExpressionTest#simpleBoolExpressionUsesLegacySelectedTableColumnAndReportParameterIndex`.
- Updated `docs/migration/foolframe-parity.md` to record the migrated `SimpleBoolExpression` behavior and the new `fool-query` Java main file count.

## TDD

- Red:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=SimpleBoolExpressionTest#simpleBoolExpressionUsesLegacySelectedTableColumnAndReportParameterIndex -DfailIfNoTests=false test`
  - Result: failed as expected at test compilation because `SimpleBoolExpression` did not exist.
- Green:
  - Same command.
  - Result: `SimpleBoolExpressionTest`: 1 test, 0 failures, 0 errors.

## Verification

- Focused query regression:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=SimpleBoolExpressionTest,QueryFactoryTest,QueryInsFacTest,QueryReportTest,QueryContextTest,ReportCompareFilterTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest -DfailIfNoTests=false test`
  - Result: 43 tests, 0 failures, 0 errors.
- Full backend package on the Compose network:
  - `docker run --rm --network fool-service_default -e SPRING_DATASOURCE_URL='jdbc:mysql://mysql:3306/car_wash?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&allowMultiQueries=true&autoReconnect=true' -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=Pa88word -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn package`
  - Result: `BUILD SUCCESS` across all 15 backend reactor modules in 38.641 s.

## Runtime Evidence

- Backend Compose image/runtime:
  - `docker compose build -q backend && docker compose up -d backend`: backend image rebuilt and container restarted.
  - `docker compose ps`: backend, frontend, mysql, and redis running; MySQL and Redis healthy.
- Runtime smoke after backend restart:
  - `GET http://localhost:8080/test`: response captured, 132 bytes.
  - `GET http://localhost:8081/`: Vue frontend response captured, 399 bytes.
  - `POST http://localhost:8080/api/v1/data/query-list` with `OrderList` `orderId` filter values `1001,1002`: response captured, 296 bytes.
  - Startup retry observed transient `curl: (56) Recv failure: Connection reset by peer`; the retry loop continued and all smoke outputs were captured.
- Repository hygiene:
  - `python scripts/check_repo_harness.py`: passed.
  - `git diff --check`: passed.
  - Touched-file trailing whitespace scan: no matches.

## Risks

- The migrated expression targets the existing Java `CompareOp` enum values. Legacy database-backed compare expressions can be richer than the current enum and remain a follow-up surface.
- This slice does not complete the full `BoolExpresstionFacotry` API or saved-query/report execution end to end.

## Follow-ups

- Continue remaining query bool-expression factory and saved-query/report execution parity.
