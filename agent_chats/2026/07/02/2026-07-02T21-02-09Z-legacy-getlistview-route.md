# Legacy getlistview Route

## Prompt

- Continue the active migration goal:
  1. run the environment with Docker;
  2. complete migration against `../FoolFrame`;
  3. use Vue for the frontend;
  4. make timely atomic commits.

## Scope

- Added legacy-compatible `POST /api/v1/view/getlistview` for view
  definition lookup by `viewId`.
- Reused the existing `ViewDataService` and `ViewAdapter` view-definition
  mapping so the new route returns the same `ListViewInfo` shape as the
  modern `/api/v1/view/get-view` endpoint.
- Added `viewId` to the backend view request DTO and Vue API request type.
- Updated migration parity notes with the new endpoint and Docker smoke route.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ViewController.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ViewDataRequest.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetListViewTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T21-02-09Z-legacy-getlistview-route.md`

## Validation

- RED:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewControllerLegacyGetListViewTest test`
  failed because `ViewDataRequest.setViewId(long)` and
  `ViewController.getListView(ViewDataRequest)` were missing.
- GREEN:
  same focused command passed with 1 test.
- Backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  passed with 46 `fool-view` tests.
- Frontend:
  `cd frontend && npm test && npm run build` passed.
- Harness:
  `python scripts/check_repo_harness.py` passed.
- Whitespace:
  `git diff --check` passed.
- Docker:
  `docker compose up -d --build backend frontend` passed.

## Runtime Evidence

- `docker compose ps` showed backend, frontend, MySQL, and Redis running.
- `curl http://localhost:8080/test` returned seeded order rows.
- `curl -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getlistview`
  returned `code:0`, `id:100`, `viewName:"OrderList"`, `type:"ListView"`,
  and seeded table columns.

## Skipped Checks

- No browser screenshot check; this change adds API compatibility and Vue API
  types, not a visible Vue UI flow.

## Risks / Follow-ups

- The route maps the legacy view-id request to the existing view-definition
  pipeline. Broader `GetViewOption` semantics beyond `ViewId` and `Token` are
  not migrated in this slice.
