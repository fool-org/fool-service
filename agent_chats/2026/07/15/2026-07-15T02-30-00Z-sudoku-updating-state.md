# Sudoku List Updating State

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `includes/List.jade`, `groupview.js`, and `querylistdata.js` before
  comparing the Vue footer lifecycle.
- Restored the old `更新中..` text while root and Group child List queries are
  pending, `FreshTime` replacement on success, and retained updating text on
  transport failure.
- Reused one ViewItem-panel key for update state and refresh timers so root
  `Orders List` and child `Group Orders`, which both use View 100, retain
  independent controller-equivalent state.
- Added no business DTO, component, route, request, or duplicate loader.

## Changed Files

- `frontend/src/useSudokuPanels.ts`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/App.vue`
- `frontend/src/SudokuPanels.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T02-30-00Z-sudoku-updating-state.md`

## Commits

- `ca95ce2c fix(frontend): show sudoku list updating state`
- `8050057a fix(frontend): retain sudoku updating on failure`
- `c2f97b97 fix(frontend): isolate sudoku panel updating state`

## Validation

- `cd frontend && npm test -- SudokuPanels.test.ts` (6 tests passed)
- `cd frontend && npm test` (20 files, 218 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (68 checks passed)
- Compose kept backend/frontend/MySQL/Redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`.
- Before the fix, a paused real root `querydata(100)` left the prior timestamp
  visible. The first deployed slice restored pending text but browser probing
  exposed the metadata-preserving `{ view, data: null }` failure shell; the
  second exposed root/Group state collision because both panels use View 100.
- Final authorized `admin/admin` acceptance waited for both List panels to
  finish initialization, paused one root refresh, and observed
  `更新时间 更新中..`; Group Orders retained its own `FreshTime`. Releasing the
  request restored the root timestamp.
- At 390x844, the next root query was rejected with `TypeError: Failed to
  fetch`. The root footer remained `更新时间 更新中..`, while Group Orders again
  retained its own timestamp. Five root panels remained mounted, document width
  equaled viewport width, and browser warnings/errors were empty.
- The token was logged out and the isolated browser tab was closed.
- MySQL retained 8 `market_order` rows and 4 `market_order_item` rows. Order
  1001 remained `BTC-USDT`, state `0`, customer `3001`, amount `0.25`, and
  price `62500`.
- Runtime image:
  `sha256:e80d0135d2dc7f565fe37ec34c4e4bee6fa762328dbb7e0d2d00c38ff863defd`.
- Visible evidence:
  - `artifacts/runs/20260715-sudoku-updating/desktop-updating.png`
    (`1280x800`, SHA-256
    `3b9d23044bb3f504cb1d82051e98755825418206bbe5f9523bd461e00da0e1d0`).
  - `artifacts/runs/20260715-sudoku-updating/mobile-failed-updating.png`
    (`390x844`, SHA-256
    `044eec8d0097026d9237369a34655e87609555f6529a4116717e32a208a2419a`).

## Risks And Follow-ups

- The standard Compose build was already confirmed unavailable in this runtime
  because local Buildx cannot update its activity file (`operation not
  permitted`). Host-validated `dist` was injected into the existing Nginx image
  for runtime acceptance.
- Screenshot files are intentionally ignored by Git; their paths and hashes are
  recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.
