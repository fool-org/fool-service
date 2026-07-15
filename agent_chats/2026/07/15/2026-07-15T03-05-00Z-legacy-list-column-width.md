# Legacy List Column Width

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Audited old `view.jade` and `querylistdata.js` before querying the imported
  View catalog and opening the deployed `User列表` View 113.
- Found all 16 data columns have `VIEW_ITEM_WIDTH=0`. PrimeVue compressed them
  and the operation column into the viewport, producing per-character wrapping.
- Kept positive View width metadata authoritative. Missing widths now use one
  shared 96-220px title-derived minimum and the operation column reserves 72px.
- Reused `ListDataTable` for main, child, candidate, and Sudoku tables. Added no
  business DTO, User special case, component, request, or route.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/ListDataTable.vue`
- `frontend/src/style.css`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/ViewListPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T03-05-00Z-legacy-list-column-width.md`

## Commits

- `645c12be fix(frontend): keep legacy metadata columns readable`

## Validation

- `cd frontend && npm test -- src/viewWorkflow.test.ts src/ViewListPanel.test.ts`
  (2 files, 51 tests passed)
- `cd frontend && npm test` (20 files, 219 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (69 checks passed)
- Java tests were not rerun because this slice changed no Java source.

## Runtime Evidence

- Before the fix, `/view113` compressed 17 rendered columns into the table
  viewport. Dates, `Male`, and `编辑` wrapped vertically at desktop; mobile
  headers were similarly squeezed.
- After the fix, View/data order remained `getlistview(113)` then
  `querydata(113)`. The table measured 1704px in both layouts, with 96-132px
  data columns and a 72px operation column. Its scroller exposed the full table
  across a 1198px desktop viewport and a 328px mobile table viewport.
- Clicking `编辑` loaded `getreaditemview(112)` before
  `querydatadetail(112,1)` and reached `/view112/1`. Clicking `新建` loaded the
  same View metadata before `initnew(112)` and reached `/new112`. Neither path
  saved data.
- A separate `/view103` replay retained all five Sudoku panels,
  `getlistview(104)` with zero `querydata(104)` calls, document widths equal to
  1280px / 390px, and empty browser logs.
- Both `docker compose build frontend` and
  `BUILDX_BUILDER=desktop-linux docker compose build frontend` failed before
  context transfer because Buildx could not update its activity file
  (`operation not permitted`). The host-validated `dist` was copied into a
  clean temporary Nginx container and committed as frontend image
  `sha256:f8c91559a1c8720a9532f2d18f349bf18a173f014c512066ede2665f271a2bf0`.
  The running container uses that exact image. Local and container `index.html`
  SHA-256 values both equal
  `b1a69f49a4a91fd2a6cfa5afd00c2db88399ac7ca7eb174a683757f49fde75bd`.
- MySQL retained one `SW_AUTH_USER`, 8 `market_order` rows, and 4
  `market_order_item` rows. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.25`, and price `62500`.
- Visible evidence:
  - `artifacts/runs/20260715-legacy-user-list/desktop-user-list.png`
    (before, `1280x900`, SHA-256
    `aa88d6c879686ed54e06bc104e493faeae3028a336e9c79ca1a81acc88063512`).
  - `artifacts/runs/20260715-legacy-user-list/mobile-user-list.png`
    (before, `390x844`, SHA-256
    `a03cbdac161b5adb81f04046a1e2443b7b0067e8309f1aa85b7ed1813115672f`).
  - `artifacts/runs/20260715-legacy-user-list/desktop-user-list-readable.png`
    (`1280x900`, SHA-256
    `8afb5b9a2201c28e667c066652dc8f9ccfee072f2bb3970529a2ab650a4ac81b`).
  - `artifacts/runs/20260715-legacy-user-list/desktop-user-list-actions.png`
    (`1280x900`, SHA-256
    `d25eed695bdb22a711581e007cabca63b9c28f5d1d76babb8de2c59a6c02bb71`).
  - `artifacts/runs/20260715-legacy-user-list/mobile-user-list-readable.png`
    (`390x844`, SHA-256
    `0c1595e02630cf0d97491e184256c785cbc2f47b8045ba7cfc607d22139d3f4b`).
  - `artifacts/runs/20260715-legacy-user-list/mobile-user-list-actions.png`
    (`390x844`, SHA-256
    `a4286a9ac2f5f20d496ac1587935c391fa47717f3fe2da48d46cf68166f708c6`).

## Risks And Follow-ups

- The standard Dockerfile image build remains blocked by the local Buildx
  activity-directory permission. Rebuild the formal image after that external
  Docker state is corrected; runtime bytes currently match the tested dist.
- Screenshot files are intentionally ignored by Git; their paths and hashes are
  recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.
