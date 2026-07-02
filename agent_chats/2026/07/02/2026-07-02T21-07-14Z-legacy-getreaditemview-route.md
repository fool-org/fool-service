# Legacy getreaditemview Route

## Prompt

- Continue the active migration goal:
  1. run the environment with Docker;
  2. complete migration against `../FoolFrame`;
  3. use Vue for the frontend;
  4. make timely atomic commits.

## Scope

- Added legacy-compatible `POST /api/v1/view/getreaditemview` for read-item
  view lookup by `viewId`.
- Added read-item DTOs for `ViewName`, `ViewId`, simple `Items`, and empty
  `DetailViews`.
- Reused `ViewDataService` and `ViewAdapter`; no new service layer was added.
- Added Vue API types for the legacy read-item payload.
- Updated migration parity notes with the new endpoint and Java file count.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/api/ViewController.java`
- `fool-view/src/main/java/org/fool/framework/view/adapter/ViewAdapter.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ReadItemViewDetailInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ReadItemViewInfo.java`
- `fool-view/src/main/java/org/fool/framework/view/dto/ReadItemViewItemInfo.java`
- `fool-view/src/test/java/org/fool/framework/view/api/ViewControllerLegacyGetReadItemViewTest.java`
- `fool-view/src/test/java/org/fool/framework/view/adapter/ViewAdapterTest.java`
- `frontend/src/api.ts`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/02/2026-07-02T21-07-14Z-legacy-getreaditemview-route.md`

## Validation

- RED:
  `docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am -Dtest=ViewAdapterTest#readItemViewIncludesLegacySimpleItems,ViewControllerLegacyGetReadItemViewTest test`
  failed because `ReadItemViewInfo` was missing.
- GREEN:
  same focused command passed with 2 tests.
- Backend:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-view -am test`
  passed.
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
- `curl -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getreaditemview`
  returned `code:0`, `viewId:100`, `viewName:"OrderList"`, simple
  read-item `items`, and empty `detailViews`.

## Skipped Checks

- No browser screenshot check; this change adds API compatibility and Vue API
  types, not a visible Vue UI flow.

## Risks / Follow-ups

- This slice covers legacy simple read-item fields. Nested array/detail view
  item expansion remains open because the current `ViewItem` model has no
  edit-view relation surface to map.
