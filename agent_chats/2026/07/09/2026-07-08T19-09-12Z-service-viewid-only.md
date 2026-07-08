# Service ViewId Only

## Prompt

Continue the FoolFrame migration and keep the data path as: render View first,
then query data by that View, without binding generic services to business DTO
names or display labels.

## Scope

- Made `ViewDataService.getViewData` reject blank/non-numeric View ids before
  DAO lookup.
- Made `DataQueryService.queryViewDataList` reject blank/non-numeric View ids
  before DAO lookup.
- Updated service tests that were using `OrderList` / `CarOwnerList` as direct
  lookup inputs to use numeric View ids instead.
- Left `InputQueryRequest.ViewName` alone because it is a legacy protocol
  compatibility boundary, not the generic View/data service entrypoint.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/ViewDataService.java`
- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataServiceTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceOrderingTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceTest.java`
- `fool-view/src/test/java/org/fool/framework/view/service/ViewDataQueryServiceTest.java`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T19-09-12Z-service-viewid-only.md`

## Validation

- RED: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewDataServiceTest,DataQueryServiceOrderingTest test`
  - Failed because `ViewDataService.getViewData("OrderList", ...)` did not
    throw and `DataQueryService.queryViewDataList("OrderList", ...)` reached
    DAO lookup before reporting a missing view.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am -Dtest=ViewDataServiceTest,DataQueryServiceOrderingTest test`
  - `17` focused tests passed.
- GREEN: `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -pl fool-view -am test`
  - `151` fool-view reactor tests passed.
- GREEN: `cd frontend && npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`
- GREEN: `docker compose up -d --build --force-recreate backend`
  - Backend image rebuilt and `fool-service-backend-1` recreated/restarted.
- GREEN: `docker compose ps`
  - Backend and frontend running; MySQL and Redis healthy.
- GREEN: `curl -sS -o /tmp/fool-service-backend-smoke.txt -w "%{http_code} %{size_download}\n" http://localhost:8080/test`
  - `200 465`.
- GREEN: `curl -sS -o /tmp/fool-service-frontend-smoke.html -w "%{http_code} %{size_download}\n" http://localhost:8081/`
  - `200 399`.

## Risks

- Direct Java service callers that still pass a View name now receive
  `ViewId is required`. That matches the controller path; callers should
  resolve View metadata by id before querying data.
