# Query runtime connection routing

## Prompt

Continue the active migration goal:

1. Bring the environment up with Docker.
2. Complete migration against `../FoolFrame`.
3. Use Vue for the frontend.
4. Make timely atomic commits.

## Scope

- Closed the legacy `QueryContext.GetResult(connectionString, pageSize)` gap.
- The Java overload now routes execution through the supplied runtime
  connection string instead of ignoring it and reusing only the configured
  executor.
- Added minimal legacy connection-string parsing for query executors:
  `Data Source=...;Initial Catalog=...;User ID=...;Password=...`.
- Updated migration parity notes for the completed query slice.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QueryContext.java`
- `fool-query/src/main/java/org/fool/framework/query/JdbcQueryExecutor.java`
- `fool-query/src/test/java/org/fool/framework/query/QueryContextTest.java`
- `fool-query/src/test/java/org/fool/framework/query/JdbcQueryExecutorTest.java`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/03/2026-07-03T05-04-57Z-query-runtime-connection.md`

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryContextTest#getResultConnectionStringOverloadUsesRuntimeConnectionFactory -DfailIfNoTests=false test`
  failed at compilation because `JdbcQueryExecutor` was not a functional
  interface. That confirmed the runtime executor factory surface was missing.
- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryContextTest#getResultWithoutExecutorOrConnectionStringKeepsRequiredExecutorError -DfailIfNoTests=false test`
  failed with a `NullPointerException`, confirming the fixed-executor
  constructor still needed to preserve the previous missing-executor error.
- GREEN focused:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryContextTest#getResultWithoutExecutorOrConnectionStringKeepsRequiredExecutorError,QueryContextTest#getResultConnectionStringOverloadUsesRuntimeConnectionFactory,JdbcQueryExecutorTest#parsesLegacyConnectionStringsForRuntimeExecutors -DfailIfNoTests=false test`
  passed 3 tests.
- Query regression:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=QueryContextTest,JdbcQueryExecutorTest,QuerySqlBuilderTest,QueryInstanceMigrationTest,QueryLookupCollectionTest,SelectTypeCatalogTest,SelectedColumnCollectionTest,SelectedTablesTest,CompareOpCatalogTest,CompareFilterTest,QueryFactoryTest,QueryInsFacTest,QueryReportTest,ReportCompareFilterTest,BoolExpressionFactoryTest,SimpleBoolExpressionTest -DfailIfNoTests=false test`
  passed 63 tests.
- Harness:
  `python scripts/check_repo_harness.py`
  passed.
- Runtime:
  `docker compose ps`
  showed backend/frontend/mysql/redis running; MySQL and Redis were healthy.
- Backend smoke:
  `curl -sS http://localhost:8080/test`
  returned seeded order rows.

## Skipped Checks

- No frontend tests or build; this slice did not touch Vue code.
- No full Maven reactor; the changed behavior is isolated to `fool-query`, and
  the broader query regression plus harness covered the touched surface.
- No backend image rebuild; runtime Java code changed, but this slice was
  validated at module level and existing Docker runtime was only used for
  environment smoke.

## Risks

- Query runtime connection creation uses a simple `DriverManagerDataSource`;
  add pooling only if real saved-query/report traffic needs it.
- Legacy SQL Server connection strings are parsed into JDBC URLs, but actual
  SQL Server driver/runtime coverage remains outside the Docker MySQL smoke.

## Follow-ups

- Continue `SWDQ01-Soway.Query` saved-query/report execution parity.
- Continue richer query-to-view integration after the runtime connection route.
