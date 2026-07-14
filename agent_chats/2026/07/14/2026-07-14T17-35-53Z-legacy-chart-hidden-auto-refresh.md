# Legacy Chart Hidden Auto Refresh

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled and Vue components bound to View metadata rather than
concrete business DTOs.

## Scope

- Compared `timer.js`, `querylistdata.js`, and `viewWithChart.js` with the Vue
  chart-tab and main View timer paths.
- Reused the existing active-tab state to report whether the metadata table is
  visible.
- Skipped only scheduled requests while a chart View's Data pane is hidden.
- Kept manual Find, normal View timers, pending-request concurrency, API
  payloads, and metadata/data projections unchanged.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/AutoRefresh.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-35-53Z-legacy-chart-hidden-auto-refresh.md`

## Validation

- `cd frontend && npm test -- --run AutoRefresh.test.ts ViewListPanel.test.ts`
  passed: 2 files, 2 tests.
- `cd frontend && npm test` passed: 19 files, 185 tests.
- `cd frontend && npm run build` passed, including `vue-tsc --noEmit`.
- `python scripts/check_repo_harness.py` passed.
- Rebuilt and deployed frontend image
  `sha256:0dbfcad41a1b686af6d94a677888356d9d2edbf5c9453e485a170a30c56acceb`;
  the running container used that exact image.
- Compose was healthy with `db-migrate` at `Exited (0)`; all 67
  `python scripts/runtime_doctor.py` checks passed.
- Temporarily changed `SW_SYS_VIEW.VIEW_AUTOFRESHINTERVAL` and
  `fool_sys_view.auto_fresh_interval` for top-level chart View 100 from zero to
  one second.
- Authorized browser acceptance on `/view100` showed consecutive one-second
  Nginx `querydata` entries while Data was selected. Switching to Chart kept
  Find active and produced zero requests in a 3.1-second window; clicking Find
  there produced exactly one request while Chart remained selected.
- Restored both metadata values to zero. A fresh `/view100` rendered the eight
  seeded rows, and a subsequent 2.6-second window produced zero `querydata`
  requests.

## Risks And Follow-ups

- The visibility gate deliberately affects only scheduled main View refreshes;
  manual Find and Sudoku panel timers keep their separate old-code paths.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
