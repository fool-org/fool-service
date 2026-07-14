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
- Pending Docker rebuild and authorized browser/runtime acceptance.

## Risks And Follow-ups

- Stop the backend after a successful `/view100` query, click Find, wait for
  the Nginx 502, and prove the eight rows/paginator remain with no error message;
  restart the backend and prove Find recovers normally.
- Candidate and Sudoku query error paths remain separate audit items because
  they have different old controller ownership and visible state.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
