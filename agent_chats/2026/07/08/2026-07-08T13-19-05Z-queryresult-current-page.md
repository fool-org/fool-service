# QueryResult Current Page Reload

## Prompt

Continue the FoolFrame migration while keeping page rendering and data loading
metadata-driven instead of binding work to the seeded order DTO.

## Scope

- Compared FoolFrame `Soway.Query.QueryResult.GetData()` with the Java
  `fool-query` result container.
- Added current-page reload behavior to `QueryResult` via `JdbcQueryExecutor`
  and the existing paged SQL builder.
- Updated migration state to stop counting this exact `GetData` current-page
  surface as open work.

## Changed Files

- `fool-query/src/main/java/org/fool/framework/query/QueryResult.java`
- `fool-query/src/main/java/org/fool/framework/query/JdbcQueryExecutor.java`
- `fool-query/src/test/java/org/fool/framework/query/JdbcQueryExecutorTest.java`
- `tasks.md`
- `docs/migration/foolframe-parity.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=JdbcQueryExecutorTest test`
  - Failed because `QueryResult.getData()` returned the original page rows
    after `setCurrentPage(3)`.
- PASS: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-query -am -Dtest=JdbcQueryExecutorTest test`

## Risks

- The reload surface reuses the current `QueryInstance` and paged SQL builder.
  Broader saved-query/report execution and query-to-view integration remain
  tracked in the migration parity document.

## Follow-ups

- Continue closing `SWDQ01-Soway.Query` surfaces with focused tests before
  touching broader View/report integration.
