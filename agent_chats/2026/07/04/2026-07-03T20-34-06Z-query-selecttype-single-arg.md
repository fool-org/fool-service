# Query SelectType Single-Argument Compatibility

## Prompt

Continue the Docker/Vue FoolFrame migration, keep changes small, and avoid
controller/DTO coupling.

## Scope

- Compared FoolFrame report execution around `HandlerMakeReport`,
  `SelectedCol`, `OrderType`, and `SqlScriptFac`.
- Fixed the shared query SQL builder so the Docker-seeded
  `SE_SELECTEDEXP` values `{0}`, `COUNT({0})`, `SUM({0})`, and similar
  single-argument forms receive the full selected column expression.
- Left report controller aggregation/execution wiring for a later slice.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QuerySqlBuilder.java`
- `fool-query/src/test/java/org/fool/framework/query/QuerySqlBuilderTest.java`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/04/2026-07-03T20-34-06Z-query-selecttype-single-arg.md`

## Validation

- RED:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -DfailIfNoTests=false -Dtest=QuerySqlBuilderTest#selectedColumnSqlSupportsSeededSingleArgumentSelectExpressions test`
  failed with `expected:< [[o].[ORDER_ID]]  AS [order_id]> but was:< [o]  AS [order_id]>`.
- GREEN:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -DfailIfNoTests=false -Dtest=QuerySqlBuilderTest#selectedColumnSqlSupportsSeededSingleArgumentSelectExpressions test`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -DfailIfNoTests=false -Dtest=QuerySqlBuilderTest test`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-query -am -DfailIfNoTests=false test`

## Skipped

- Did not wire `/api/v1/report/getrpt` to build and execute a full
  `QueryInstance`; that needs its own red/green slice because ordering and
  paging must happen before result rows are rendered.

## Risks

- The compatibility is proven at the builder level. Runtime report aggregation
  remains incomplete until the report endpoint uses the shared query execution
  path.

## Follow-ups

- Build `getrpt` selected-column execution through View-derived query metadata,
  including `SelectedTypeId` and `OrderType`, without page-local row sorting.
