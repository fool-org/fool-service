# Legacy List Row And Paging Availability

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits without coupling Vue components to concrete business DTOs.

## Scope

- Compared `view.jade`, `querylistdata.js`, and `navbar.js` row and pagination
  commands with the Vue main list.
- Removed the Vue-only global request lock from main-list row navigation and
  the shared legacy paginator.
- Kept page-boundary behavior and Sudoku table/refresh pending guards.
- Made the shared table's existing disabled prop default to false and removed
  redundant false props from the already-active detail candidate controls.
- Added no state, request path, route, DTO binding, or duplicate component.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/LegacyPagination.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `frontend/src/ViewListPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T16-56-15Z-legacy-list-row-paging-availability.md`

## Validation

- `cd frontend && npm test` passed: 16 files, 182 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- The focused `ViewListPanel.test.ts` contract is 24 lines;
  `LegacyPagination.vue` shrank to 40 lines and `ViewDetailPanel.vue` to 459.
- `docker compose build frontend` passed.
- `docker compose up -d --no-deps --force-recreate frontend` passed.
- Deployed frontend image:
  `sha256:2b2b2ea132c14025fe150d8e4fe43e74a6c5d2cb529444d3416f143e52c0a089`.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- `python scripts/runtime_doctor.py` passed all 67 checks before and after
  browser acceptance; backend `/test` passed through the doctor.
- Authorized browser acceptance temporarily inserted orders `990001` through
  `990003`, raising the list from eight to eleven rows and exposing Page 2.
- While a real paused-backend `getsubmenu` request was pending, Page 2 retained
  `disabled=false`, switched to the selected state, and loaded order `1001 /
  BTC-USDT` after the backend resumed.
- Runtime operations had no target View, so operation `7002` was temporarily
  changed from result View `NULL` to `102`. During a separate paused-backend
  list query, row `1001`'s Save-labelled link retained `disabled=false` and
  navigated directly from `/view100` to `/view102/1001`; no save or operation
  request was submitted.
- Operation `7002` was restored to result View `NULL`, the three temporary rows
  were deleted, and MySQL returned to eight orders. Reloaded `/view100` showed
  eight records, no Page 2, no active Save row buttons, and no temporary text.
- Order `1001` remained `BTC-USDT / customer 3001 / state 0`.

## Risks And Follow-ups

- This parity slice is closed; the broader old-page migration remains active.
- No screenshot artifact was retained because the acceptance depended on DOM
  enabled state and temporary request/database state; final runtime and data
  restoration were verified directly.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
