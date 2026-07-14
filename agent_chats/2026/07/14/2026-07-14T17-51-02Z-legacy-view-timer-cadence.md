# Legacy View Timer Cadence

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping changes small and View-first.

## Scope

- Compared `timer.js` registration/counting and `querylistdata.js` interval
  watch with the Vue main View timer lifetime.
- Reused one timer when response metadata keeps the same interval.
- Matched the old one-second due-check/increment sequence.
- Kept the timer across manual Find, paging, and same-interval responses.
- Preserved View-navigation cleanup, interval-change replacement, hidden-chart
  gating, pending-request concurrency, and scheduled page-one reset.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/AutoRefresh.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-51-02Z-legacy-view-timer-cadence.md`

## Validation

- `cd frontend && npm test -- --run AutoRefresh.test.ts payload.test.ts`
  passed: 2 files, 83 tests.
- `cd frontend && npm test` passed: 19 files, 185 tests.
- `cd frontend && npm run build` passed, including `vue-tsc --noEmit`.
- `python scripts/check_repo_harness.py` passed.
- Rebuilt and deployed frontend image
  `sha256:67ee985fe976be13e5c12b573f0716b63b5d5e05b6a12185e7490f014f5a7b02`;
  the running container used that exact image.
- Temporarily changed both View 100 interval rows from zero to one second.
- Authorized browser acceptance produced an initial Nginx `querydata` entry at
  `17:54:19.215`, the first timer entry at `17:54:21.259`, then entries at
  `22.257`, `23.245`, and `24.245`. This proves the old zero-counter first delay
  and subsequent one-second cadence without response-driven re-registration.
- Paused the backend, clicked Find, and left the Data tab visible. Find stayed
  active, and 18 manual/scheduled requests completed together when the backend
  resumed, proving Find did not stop or replace the active ticker.
- Restored both interval rows to zero. The active page consumed that metadata,
  and a subsequent 2.6-second window contained zero `querydata` requests.
- View 100's file/interval metadata finished at 990001/0, compatibility
  interval at zero, and order/item counts at 8/4.
- Compose was healthy with `db-migrate` at `Exited (0)`; all 67
  `python scripts/runtime_doctor.py` checks passed.

## Risks And Follow-ups

- The implementation intentionally preserves FoolFrame's delayed first tick;
  interval one first fires after about two seconds, then once per second.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
