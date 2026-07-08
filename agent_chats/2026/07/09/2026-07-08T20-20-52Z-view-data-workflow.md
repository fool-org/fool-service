# View Data Workflow

## Prompt

先查 view 渲染页面，再根据 view 查 data，和具体的业务 DTO 绑定 是有问题的。

## Scope

- Added `frontend/src/useViewDataWorkflow.ts` as the shared Vue workflow for
  legacy `getlistview`, `getreaditemview`, and `querydata` calls.
- Kept rendered list columns sourced from View metadata and data rows sourced
  from generic `ListDataItem` values, not concrete order/market DTO fields.
- Moved duplicated View/data response state, loaded View id synchronization,
  read-item View cache, and list paging totals out of `App.vue`.
- Left child existing-row selection on the same View-first path: load child
  list View, query child data, then render candidate columns from the child
  View metadata.

## Changed Files

- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/useViewDataWorkflow.test.ts`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-08T20-20-52Z-view-data-workflow.md`

## Validation

- GREEN: `cd frontend && npm test`
  - 5 files and 99 tests passed, including the new View/data workflow tests.
- GREEN: `cd frontend && npm run build`
  - `vue-tsc --noEmit` and Vite production build passed.
- GREEN: `docker compose up -d --build frontend`
  - Compose rebuilt frontend and, through the dependency chain, rebuilt and
    recreated backend; both containers started.
- GREEN: `python scripts/runtime_doctor.py`
  - Compose, auth shell, View/data, inputquery, report, message, notify, and
    logout probes passed.
- GREEN: `docker compose ps`
  - Backend and frontend were running; MySQL and Redis were healthy.
- GREEN: `curl http://localhost:8081/`
  - Returned `200 399` and referenced the rebuilt `index-BpCmy0mu.js` bundle.
- GREEN: `curl http://localhost:8080/test`
  - Returned `200 465`.
- GREEN: Playwright with system Chrome against `http://localhost:8081/`
  - Rendered one table plus Detail panel, observed `getlistview` before
    `querydata`, followed by `getreaditemview`, `querydatadetail`, and
    `getenums`; no page errors were captured.

## Risks

- The first-screen Vue workflow is now View-first and usable for the seeded
  Order List, but `App.vue` remains large. Further UI migration should continue
  by extracting cohesive workflow helpers instead of adding more state to the
  root component.
