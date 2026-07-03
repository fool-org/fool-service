# Vue ViewId Workflow

## Prompt

- Keep the FoolFrame migration focused on the View-rendered page first, then
  query data from that View.
- Avoid binding the usable Vue workflow to concrete business DTO fields.
- Keep files small and reuse the existing frontend request builders.

## Scope

- Changed the first-screen Vue workflow to load View metadata with legacy
  `getlistview(viewId)` and then query rows with legacy `querydata(viewId)`.
- Kept `/api/v1/view/get-view` and `/api/v1/data/query-list` in API Tools for
  compatibility/debugging, but stopped using them as the main page workflow.
- Synced the loaded `viewName` from returned View metadata so lookup editors
  still send the View-defined name after the page starts from `viewId`.
- Updated the workflow toolbar to show `View ID`, matching the route actually
  used by the migrated FoolFrame flow.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T18-36-24Z-vue-viewid-workflow.md`

## Validation

- `cd frontend && npm test -- payload.test.ts`
  - Passed: 34 tests.
- `cd frontend && npm test`
  - Passed: 3 test files, 43 tests.
- `git diff --check`
  - Passed.
- `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit` and Vite production build.
- `docker compose up -d --build frontend`
  - Passed. Compose also rebuilt the backend dependency image with
    `mvn -DskipTests package`; Maven reactor finished `BUILD SUCCESS`.

## Runtime Evidence

- `docker compose ps`
  - backend: running on `0.0.0.0:8080->8080`
  - frontend: running on `0.0.0.0:8081->80`
  - mysql: healthy on `127.0.0.1:3307->3306`
  - redis: healthy on `127.0.0.1:6380->6379`
- Frontend bundle check:
  - Served bundle from `http://localhost:8081/`.
  - Bundle contains `workflow-query`, `/api/v1/data/querydata`,
    `getlistview`, and `View ID`.
- View metadata route:
  `curl -fsS -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getlistview`
  - Result: `code=0`, `viewId=100`, `viewName=OrderList`, `columns=4`.
- Data route from the same View:
  `curl -fsS -H 'Content-Type: application/json' -d '{"viewId":100,"pageSize":20,"pageIndex":1}' http://localhost:8080/api/v1/data/querydata`
  - Result: `code=0`, `totalItem=8`, `rows=8`.
  - First row columns came from View data items:
    `orderId`, `symbol`, `customer`, `state`.

## Skipped Checks

- Full backend `mvn test` was not run; this slice only changed Vue workflow
  wiring and its source-level test. Docker build did compile/package backend
  with tests skipped because Compose rebuilt the dependency image.
- Browser click-through was not repeated for this tiny workflow routing slice;
  route proof used the rebuilt frontend bundle plus live backend curl checks.

## Risks

- API Tools still expose the newer `viewName/query-list` path by design. The
  main migrated first-screen workflow no longer depends on it.
