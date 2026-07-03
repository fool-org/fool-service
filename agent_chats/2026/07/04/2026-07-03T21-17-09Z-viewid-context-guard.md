# ViewId Context Guard

## Prompt

The migrated page should render from View metadata first, then query data from
that View context. Binding the runtime flow back to concrete business DTOs or
manual ViewName/detail IDs is the wrong direction.

## Scope

- Added `ViewId` support to the generic `QueryDataRequest`.
- Changed generic `query-list` and `get-view` controllers to prefer `ViewId`
  before legacy `ViewName`.
- Stopped the Vue main metadata workflow from sending `viewName` with
  `inputquery`.
- Forced main detail refresh, operation refresh, object save refresh, and child
  row save refresh to use the currently loaded `currentViewId`.
- Preserved compatibility fallbacks; no new business-specific DTO or trading
  defaults were introduced.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/dto/QueryDataRequest.java`
- `fool-view/src/main/java/org/fool/framework/view/api/DataController.java`
- `fool-view/src/main/java/org/fool/framework/view/api/ViewController.java`
- `fool-view/src/test/java/org/fool/framework/view/api/DataControllerLegacyQueryDataTest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetListViewTest.java`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/04/2026-07-03T21-17-09Z-viewid-context-guard.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -q -pl fool-view -am -DfailIfNoTests=false -Dtest=DataControllerLegacyQueryDataTest,ViewControllerLegacyGetListViewTest test`
- `python3 scripts/check_repo_harness.py`
- `cd frontend && npm test`
- `cd frontend && npm run build`
- `git diff --check`
- `docker compose up -d --build`
- `python3 scripts/runtime_doctor.py`

## Runtime Evidence

- `docker compose up -d --build` rebuilt backend and frontend images and
  restarted the Compose stack.
- `docker compose ps` showed backend and frontend running on ports `8080` and
  `8081`, with MySQL and Redis healthy.
- `python3 scripts/runtime_doctor.py` returned `PASS` for compose services,
  backend `/test`, frontend-proxied `getlistview(ViewId=100)`,
  `querydata(ViewId=100)`, `inputquery(ViewId=100)`, and
  `getmkqview(ViewId=100)`.

## Risks

- The main Vue file is still large. This change deliberately avoided a broad
  component split and only reduced context leakage; future frontend work should
  keep extracting helpers instead of expanding `App.vue`.
