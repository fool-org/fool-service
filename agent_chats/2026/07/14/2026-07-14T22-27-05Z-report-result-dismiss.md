# Report Result Dismiss Parity

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced the static result modal in `view.jade`, Bootstrap 3.3.5 modal defaults,
  `MakeReportController.mkrpt`, and `ShowReportController.back` before changing
  Vue.
- Restored mask/Escape result dismissal so a later `统计` opens setup instead of
  redisplaying stale result content.
- Preserved the current report page on passive dismissal. Explicit Return still
  owns the old page-index reset to one.
- Added one component-local close function. Added no App state, report DTO,
  route, dependency, or helper abstraction. `ViewReportPanel.vue` remains 414
  lines.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T22-27-05Z-report-result-dismiss.md`

## Validation

- `cd frontend && npx vitest run src/ViewReportPanel.test.ts
  src/payload.test.ts` (2 files, 97 tests passed)
- `cd frontend && npm test` (20 files, 212 tests passed)
- `cd frontend && npm run build`
- `git diff --check`
- `python scripts/runtime_doctor.py` (68 checks passed)
- `python scripts/check_repo_harness.py` was attempted but failed on unrelated
  concurrent work: `AppManageMigrationTest.java` has 2106 lines against the
  repository limit of 2100.
- Exact implementation commit `cb2b23d0` produced deployed image
  `sha256:201af77f6aa3cd9762204e91d3ea842bf0904f9d1e2502507916f409ded2a51d`
  with report chunk `ViewReportPanel-eJYK5ZH5.js`. The image returned HTTP 200
  for the frontend and proxied backend on port 8087.
- Authorized isolated-browser acceptance read a fresh local CAPTCHA, logged in
  with `admin/admin`, opened `/view101`, and used HTTP-200 report metadata plus
  a two-page HTTP-200 report response.
- Initial Confirm requested page 1 and Next requested page 2. Clicking the
  result mask closed the modal; a fresh `统计` metadata request reopened setup,
  and Confirm requested page 2 again. Explicit Return then made the next Confirm
  request page 1. The exact report page sequence was `[1,2,2,1]`.
- Logout returned HTTP 200 with no browser console or page errors.
- Visible evidence:
  `artifacts/runs/20260715-report-result-dismiss/setup-after-result-mask.png`
  (`1440x900`, SHA-256
  `57de37714df8d60e4ff55afaf7617ec62e8f3d728e399231721948ca52a6d3cd`).
- Compose retained backend/frontend/mysql/redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.
- Post-acceptance MySQL checks retained 8 `market_order` rows and 4
  `market_order_item` rows. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.2500000000`, and price `62500.0000000000`.

## Risks And Follow-ups

- The page-sequence assertion used synthetic HTTP-200 report responses while
  authentication, View routing, modal transitions, paging commands, and logout
  remained live.
- The screenshot artifact is intentionally ignored by Git; its exact path and
  hash are recorded above.
- Repository harness status remains blocked only by the concurrent oversized
  Java test noted above; frontend, exact-image, browser, runtime-doctor, Compose,
  and database checks passed.
