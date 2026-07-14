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
- Pending Docker rebuild and authorized browser/runtime acceptance.

## Risks And Follow-ups

- Temporarily set the seeded top-level chart View interval to one second,
  compare Data-tab and Chart-tab request counts, prove manual Find still sends
  a request on Chart, then restore the exact original metadata values.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
