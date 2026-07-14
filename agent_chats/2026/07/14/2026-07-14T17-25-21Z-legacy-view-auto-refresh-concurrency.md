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
- Docker image
  `sha256:5e8bb2a8e6cbb7167f035ad884c171ff5ba577fdeb63d627a396ca81741ef391`
  was rebuilt and deployed.
- Compose was healthy with `db-migrate` at `Exited (0)`; all 67
  `python scripts/runtime_doctor.py` checks passed.
- Temporarily changed `SW_SYS_VIEW.VIEW_AUTOFRESHINTERVAL` and
  `fool_sys_view.auto_fresh_interval` for View 100 from zero to one second.
- On authorized `/view100`, paused the backend, clicked Find, and waited 2.6
  seconds. Find stayed active, no error dialog appeared, and the frontend
  Nginx log recorded 36 `querydata` requests after the acceptance marker,
  proving scheduled calls were not skipped behind the pending manual request.
- Restored both metadata values to zero. A fresh `/view100` load showed the
  eight seeded rows, and a subsequent 2.6-second log window contained zero
  `querydata` requests.

## Risks And Follow-ups

- The concurrency behavior intentionally allows overlapping View requests,
  matching the old timer contract; slow responses can therefore complete out
  of order as they could in FoolFrame.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
