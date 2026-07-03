# Vue Generic View Workflow

## Prompt

- User corrected the migration direction: render the page from View metadata
  first, then query data by that View, and avoid binding the frontend to a
  concrete business DTO such as the Docker seed order data.
- Keep file sizes and reuse under control.

## Scope

- Kept the main Vue workflow on the legacy `getlistview(viewId)` then
  `querydata(viewId)` sequence.
- Removed first-screen and API-tool defaults that made the app look bound to
  `OrderList`, trading symbols, or order-state filters.
- Renamed the primary frontend section/classes from order semantics to View
  semantics.
- Replaced the backend smoke table's hardcoded `order_price` columns with a
  generic record-key renderer.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/04/2026-07-03T20-02-59Z-vue-generic-view-workflow.md`

## Validation

- Red evidence:
  `cd frontend && npm test -- viewWorkflow.test.ts` failed before
  implementation with `recordColumns is not a function`.
- Green frontend tests:
  `cd frontend && npm test`
- Green frontend build:
  `cd frontend && npm run build`

## Runtime Evidence

- Docker rebuild:
  `docker compose up -d --build --force-recreate frontend`
  completed successfully. Compose rebuilt both frontend and backend images
  because of service/build dependencies, then restarted backend and frontend.
- `docker compose ps` showed backend on `8080`, frontend on `8081`, MySQL
  healthy, and Redis healthy.
- `curl -fsS http://localhost:8081` returned the Vue `index.html`.
- `curl -fsS http://localhost:8081/test` returned backend seed records through
  the frontend proxy.
- `curl -fsS -H 'Content-Type: application/json' -d '{"ViewId":100}' http://localhost:8081/api/v1/view/getlistview`
  returned `code=0`, `viewName=OrderList`, and populated `tableColumn`.
- `curl -fsS -H 'Content-Type: application/json' -d '{"ViewId":100,"PageSize":2,"PageIndex":1}' http://localhost:8081/api/v1/data/querydata`
  returned `code=0`, page metadata, and View-shaped rows with `items`.

## Skipped Checks

- Backend Maven tests were not rerun because this slice only changes Vue
  source/tests and migration docs.

## Risks / Follow-ups

- Docker seed `ViewId=100` still resolves to the legacy `OrderList` sample data;
  that is seed content, not a frontend DTO binding.
- Broader report persistence/execution/export parity remains open.
