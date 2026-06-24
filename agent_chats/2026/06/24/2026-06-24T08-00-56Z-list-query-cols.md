# List Query Columns Compatibility Surface

## Prompt

- Continue the `/goal`: run the Docker environment, migrate against `../FoolFrame`,
  keep the frontend on Vue, and make timely atomic commits.

## Scope

- Compared `../FoolFrame/src/Server/Soway.Server/Querylist/ResultQuery.cs`
  and `HandlerQueryData.cs`.
- Legacy list-query responses include `Cols`, and `HandlerQueryData` excludes
  `ItemEditType.Format` view items from those columns.
- Added `cols` to Java `ListViewResult`, populated from non-format view items.
- Synced Vue API TypeScript types for recently migrated view metadata:
  operations, row format, and list-query columns.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ListViewResult.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `frontend/src/api.ts`
- `agent_chats/2026/06/24/2026-06-24T08-00-56Z-list-query-cols.md`

## Validation

- Red test first:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataAdapterTest#formatViewItemBecomesLegacyRowFmtAndIsExcludedFromValues -DfailIfNoTests=false test`
  - Failed compiling because `ListViewResult.getCols()` did not exist.
- Focused green:
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataAdapterTest -DfailIfNoTests=false test`
  - `docker run --rm -v "$PWD":/workspace -w /workspace -v "$HOME/.m2":/root/.m2 maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -Dtest=ViewDataAdapterTest,ViewAdapterTest,LegacyAutoViewFactoryTest -DfailIfNoTests=false test`
- Frontend checks:
  - `cd frontend && npm test`
  - `cd frontend && npm run build`
- Docker rebuild:
  - `docker compose up -d --build`
- Runtime smoke:
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Response included `"cols":["Order ID","Symbol","State"]`.
  - `curl -fsS --retry 10 --retry-delay 3 --retry-connrefused http://localhost:8081/`

## Skipped Checks

- Full backend `mvn test` was not run; this slice is isolated to `fool-view`
  adapter/DTO behavior plus Vue API type declarations.

## Risks And Follow-Ups

- Other legacy `ResultQuery` fields such as `FreshTime` and `AutoFreshTime`
  remain unmigrated because current Java `View` metadata does not yet carry
  auto-refresh interval data.
