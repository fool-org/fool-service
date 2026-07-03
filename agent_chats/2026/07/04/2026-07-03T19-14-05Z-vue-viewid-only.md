# Vue ViewId Only Workspace

## Prompt

- The Vue page should render from View metadata first, then query data from
  that View.
- Binding the page to concrete business DTOs or business-name shortcuts is a
  migration problem.
- Keep the change small, reuse existing code, and keep file size under control.

## Scope

- Removed the visible Vue workspace shortcuts for
  `/api/v1/view/get-view` and `/api/v1/data/query-list`.
- Removed the `loadView()` / `queryData()` frontend functions and their
  quick-filter state, leaving the visible View/data workflow on
  `getlistview(viewId)` followed by `querydata(viewId)`.
- Kept backend compatibility routes and payload builders untouched.
- Reused `rowValue(row, column)` in the tools result table so rendered cells
  follow the View-data item helpers instead of directly indexing
  `row.values[column.property]`.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T19-14-05Z-vue-viewid-only.md`

## Validation

- Red test:
  `cd frontend && npm test -- --run src/payload.test.ts`
  - Failed as expected because `App.vue` still contained
    `/api/v1/view/get-view`.
- Focused frontend test:
  `cd frontend && npm test -- --run src/payload.test.ts`
  - Passed: 36 tests.
- Full frontend:
  `cd frontend && npm test`
  - Passed: 3 test files, 45 tests.
- Frontend build:
  `cd frontend && npm run build`
  - Passed: `vue-tsc --noEmit` and Vite production build.
- Whitespace:
  `git diff --check`
  - Passed.
- Docker frontend:
  `docker compose build --quiet frontend`
  `docker compose up -d --no-deps --force-recreate frontend`
  - Passed.

## Runtime Evidence

- Served frontend root:
  `curl -fsS http://localhost:8081/`
  - Returned the Vue shell HTML with asset `index-BH-6o7a2.js`.
- Rebuilt frontend bundle:
  `docker compose exec -T frontend sh -lc 'if grep -R "/api/v1/view/get-view\|/api/v1/data/query-list" -n /usr/share/nginx/html; then exit 1; else echo no-old-viewname-routes; fi'`
  - Returned `no-old-viewname-routes`.
- Rebuilt frontend bundle:
  `docker compose exec -T frontend sh -lc 'grep -R "/api/v1/view/getlistview\|/api/v1/data/querydata" -n /usr/share/nginx/html | head'`
  - Found the legacy view/data routes in the served JS bundle.
- View metadata route:
  `curl -fsS -H 'Content-Type: application/json' -d '{"viewId":100}' http://localhost:8080/api/v1/view/getlistview`
  - Returned `code=0`, `viewId=100`, `viewName=OrderList`, four columns, and
    operations `7001` and `7002`.
- Data route from the same View:
  `curl -fsS -H 'Content-Type: application/json' -d '{"viewId":100,"pageSize":20,"pageIndex":1}' http://localhost:8080/api/v1/data/querydata`
  - Returned `code=0`, `totalItem=8`, and rows with View-data `items[]`
    metadata including `orderId`, `symbol`, `customer`, and `state`.

## Skipped Checks

- Backend Maven tests were not rerun because this slice only changed Vue
  wiring, docs, and frontend tests.
- Browser click automation was not added; runtime proof used the served bundle
  plus live backend View/data route checks.

## Risks

- The backend compatibility routes and `buildQueryRequest` still exist for
  non-workspace callers. The visible Vue workspace no longer exposes or calls
  those paths.
