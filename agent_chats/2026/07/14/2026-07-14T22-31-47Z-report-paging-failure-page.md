# Report Paging Failure Page State

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `ShowReportController.next/pre` and `$rootScope.mkreport` before
  changing Vue. The old controller changes `pageindex` before the request, and
  a successful response replaces cells/total pages without replacing that
  index.
- Rendered the View report's component request page instead of preferring stale
  response `CurrentPage` after a transport failure.
- Removed the now-unused response-page adapter and its unit assertion rather
  than retaining dead compatibility code.
- Added no report DTO, request field, route, component, fallback, dependency, or
  abstraction. `ViewReportPanel.vue` remains 413 lines.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T22-31-47Z-report-paging-failure-page.md`

## Validation

- `cd frontend && npx vitest run src/ViewReportPanel.test.ts
  src/viewWorkflow.test.ts src/payload.test.ts` (3 files, 144 tests passed)
- `cd frontend && npm test` (20 files, 212 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `python scripts/runtime_doctor.py` (68 checks passed)
- Exact implementation commit `b6ea15fd` produced deployed image
  `sha256:62ebb6e74f03da62d9d6e14353da843445006f73cb20b992ea7fe52ffe731caf`
  with report chunk `ViewReportPanel-Bc-_HvrS.js`. The image returned HTTP 200
  for the frontend and proxied backend on port 8088.
- Authorized isolated-browser acceptance read a fresh local CAPTCHA, logged in
  with `admin/admin`, opened `/view101`, and used a two-page HTTP-200 report.
- The page-2 report request was forced to HTTP 502. The heading became
  `报表结果 共2页 当前第2页` while the table retained `页码 / 1`; no shared error
  dialog appeared. A second Next emitted no request, while Previous requested
  page 1 and restored its heading/table. Report requests were `[1,2,1]`.
- Logout returned HTTP 200 with no page exceptions. The browser console had one
  expected resource error for the forced HTTP 502 and no application errors.
- Visible evidence:
  `artifacts/runs/20260715-report-paging-failure-page/page-advanced-table-retained.png`
  (`1440x900`, SHA-256
  `7a845e241983ad4c52e6c4fa2a3b192658e39a8218b1d8cf940287ff5c6f581e`).
- Compose retained backend/frontend/mysql/redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.
- Post-acceptance MySQL checks retained 8 `market_order` rows and 4
  `market_order_item` rows. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.2500000000`, and price `62500.0000000000`.

## Risks And Follow-ups

- The two-page result and HTTP 502 were synthetic while authentication, View
  routing, report setup/result rendering, paging commands, and logout remained
  live.
- The screenshot artifact is intentionally ignored by Git; its exact path and
  hash are recorded above.
