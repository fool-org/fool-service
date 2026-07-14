# Legacy View Auto Refresh Concurrency

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits without coupling Vue components to concrete business DTOs.

## Scope

- Compared `timer.js` and `querylistdata.js` registration with the Vue main
  View auto-refresh callback.
- Removed the Vue-only skip while another request is pending.
- Kept metadata-driven interval selection, timer cleanup, page-one reset, and
  the shared View query path.
- Added no state, request type, route, DTO binding, or duplicate component.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/AutoRefresh.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-25-21Z-legacy-view-auto-refresh-concurrency.md`

## Validation

- `cd frontend && npm test` passed: 19 files, 185 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- The focused `AutoRefresh.test.ts` contract is 15 lines; App lost one
  request-skipping conditional.
- Pending Docker rebuild, runtime doctor, and authorized browser/runtime
  acceptance.

## Risks And Follow-ups

- Temporarily set View 100's refresh interval to one second, prove multiple
  `querydata` calls while another is paused, then restore the exact original
  metadata value.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
