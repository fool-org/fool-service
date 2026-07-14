# Legacy View Query HTTP Error

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping changes reusable and View-first.

## Scope

- Compared `querylistdata.js`'s success-only query request with Vue's shared
  request-error presentation.
- Classified network, non-2xx, and empty-body failures as transport errors in
  the shared API boundary.
- Added an optional reusable `silentTransport` action policy.
- Applied that policy only to the main View `querydata` action.
- Kept response-backed business errors and every other action's error behavior.

## Changed Files

- `frontend/src/api.ts`
- `frontend/src/App.vue`
- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/api.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-59-19Z-legacy-view-query-http-error.md`

## Validation

- `cd frontend && npm test -- --run api.test.ts payload.test.ts
  useViewDataWorkflow.test.ts` passed: 3 files, 91 tests.
- `cd frontend && npm test` passed: 19 files, 186 tests.
- `cd frontend && npm run build` passed, including `vue-tsc --noEmit`.
- `python scripts/check_repo_harness.py` passed.
- Compose rebuilt and replaced the frontend with image
  `sha256:17e563ff5095bb9226a916f3c3b61c0664a6dd7f9072498a147cb2369764dc4e`;
  the running container references the same image.
- Authorized browser acceptance loaded `/main` with the eight seeded rows,
  stopped the backend, and clicked Find. Nginx recorded `POST
  /api/v1/data/querydata` as `502`; the eight rows and paginator remained,
  Find was enabled after settlement, and no `HTTP 502`, `Failed to fetch`, or
  shared `发生错误` message appeared.
- After backend restart, Find succeeded in the same authenticated session and
  retained the eight-row result with no error residue.
- `docker compose ps -a` passed: backend/frontend/MySQL/Redis were running,
  MySQL/Redis were healthy, and `db-migrate` was `Exited (0)`.
- Runtime restoration checks passed: `SW_SYS_VIEW` View 100 remained file
  `990001` with refresh interval `0`, `fool_sys_view` View 100 remained at
  interval `0`, and `market_order` / `market_order_item` counts remained 8/4.
- `python scripts/runtime_doctor.py` passed all 67 checks.

## Risks And Follow-ups

- Candidate and Sudoku query error paths remain separate audit items because
  they have different old controller ownership and visible state.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
