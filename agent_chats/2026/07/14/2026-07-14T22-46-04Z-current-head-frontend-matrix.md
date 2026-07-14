# Current HEAD Frontend Matrix

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Re-audited every old Jade template event against the current Vue controls;
  no additional source change was justified after the latest login/report
  lifecycle commits.
- Ran an authenticated, read-only browser matrix across the main legacy route
  shapes on the current frontend image.
- Verified View metadata request ordering, report state, desktop/mobile
  containment, browser errors, and database identity without saving data.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T22-46-04Z-current-head-frontend-matrix.md`

## Validation

- Exact HEAD `fd513c53` used deployed frontend image
  `sha256:8d8a2cabf1887e8699cb9c2f6c1428e93505d78d2010a3ff16e415403a674d2c`.
- `cd frontend && npm test -- --run payload.test.ts` (85 tests passed)
- `cd frontend && npm test` (20 files, 212 tests passed)
- `cd frontend && npm run build`
- `python scripts/runtime_doctor.py` (68 checks passed)
- `python scripts/check_repo_harness.py`
- Authorized `admin/admin` browser acceptance loaded `/view100`, `/view101`,
  `/view102`, `/view103`, `/itemview100`, `/view100/1001`, and `/new100`.
- List routes requested `getlistview` before `querydata`; detail requested
  `getreaditemview` before `querydatadetail`; new requested
  `getreaditemview` before `initnew`; Sudoku loaded View metadata before each
  panel's data. All requests returned HTTP 200.
- View 101 report setup selected `Item Name / 计数`, generated
  `Item Name[计数] = 4`, and returned with candidate, method, and output intact.
  Only one HTTP-200 `getmkqview` request was emitted.
- Every tested desktop page had `document.scrollWidth = innerWidth = 1440`.
  At 390x844, Sudoku and detail both had
  `document.scrollWidth = innerWidth = 390`; Sudoku rendered three tables plus
  chart/map/item/group panels and detail rendered its Items table.
- Across login, all routes, report, and logout, all 65 observed API requests
  succeeded; browser console and page errors were empty.
- Compose retained backend/frontend/mysql/redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.
- MySQL retained 8 `market_order` rows and 4 `market_order_item` rows. Order
  1001 remained `BTC-USDT`, state `0`, customer `3001`, amount
  `0.2500000000`, and price `62500.0000000000`.
- Visible evidence:
  - `artifacts/runs/20260715-frontend-head-acceptance/desktop-report-return.png`
    (`1440x900`, SHA-256
    `2a75159cb5eac15d40c41b4f12f11737920c16e2730766f8382d99c0ac9a6243`).
  - `artifacts/runs/20260715-frontend-head-acceptance/mobile-sudoku.png`
    (`390px` viewport, SHA-256
    `84ded2fd92eb6d6706e20b480151dc61fe2045d16d3461eaa7c07d524e6e7f86`).
  - `artifacts/runs/20260715-frontend-head-acceptance/mobile-detail.png`
    (`390px` viewport, SHA-256
    `e344ff51d5edd13c322f1cacb1b74ddb6dbe758240c0a6d06f75e778ab5ea20f`).

## Risks And Follow-ups

- Screenshot artifacts are intentionally ignored by Git; exact paths and
  hashes are recorded above.
- Static `/about` and `/contact` still use the intentional SPA fallback because
  the FoolFrame pages contain only broken Visual Studio placeholder content and
  no product workflow.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.
