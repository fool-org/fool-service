# Delivery Evidence: legacy list-query paging aliases

## Prompt

- Continue the Docker/Vue migration from `../FoolFrame` and keep changes in atomic commits.

## Scope

- Add current-JSON aliases for legacy `Soway.Service.ResultQuery` top-level fields:
  - `TotalItem` -> `totalItem`
  - `TotalPage` -> `totalPage`
  - `PageIndex` -> `pageIndex`
  - `Data` -> `data`
- Keep the existing `pageInfo` and `items` fields for the Vue workflow.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/ListViewResult.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewDataAdapter.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewDataAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`

## Validation

- RED:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewDataAdapterTest#listResultIncludesLegacyPagingAliasesAndDataAlias test`
  - Failed as expected: `ListViewResult should expose legacy totalItem`.
- GREEN focused:
  - Same command passed.
- Module:
  - `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  - Result: `BUILD SUCCESS`; `Tests run: 37, Failures: 0, Errors: 0, Skipped: 0`.
- Frontend:
  - `cd frontend && npm test && npm run build`
  - Result: Vitest 3 tests passed; `vue-tsc && vite build` passed.
- Runtime:
  - `docker compose up -d --build backend`
  - `docker compose ps`
  - `curl -fsS --retry 10 --retry-delay 2 --retry-connrefused http://localhost:8080/test`
  - `curl -fsS --retry 10 --retry-delay 2 --retry-connrefused -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
  - Result: backend/frontend/MySQL/Redis running; `query-list` returned `totalItem:2`, `totalPage:1`, `pageIndex:1`, and `data` matching `items`.

## Skipped Checks

- Full root `mvn test` was not rerun; this slice is isolated to `fool-view` response DTO/adapter and Vue typing.

## Risks / Follow-ups

- This keeps the existing lower-camel JSON style. It does not add exact legacy uppercase JSON property names like `TotalItem`.
