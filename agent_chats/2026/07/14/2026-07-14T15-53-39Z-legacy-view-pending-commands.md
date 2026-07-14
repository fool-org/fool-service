# Legacy View Pending Commands

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits.

## Scope

- Compared the top forms in `view.jade` and `viewWithChart.jade` with the shared
  Vue `ViewListPanel`.
- Removed the Vue-only pending disable from Find and metadata create commands.
- Kept Report's missing-View initialization guard while removing its global
  request-pending condition.
- Retained the existing pending boundary for shared row actions, Sudoku panels,
  pagination, detail, and report internals.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T15-53-39Z-legacy-view-pending-commands.md`

## Validation

- `cd frontend && npm test` passed: 14 files, 179 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- `docker compose build frontend` passed.
- `docker compose up -d --no-deps --force-recreate frontend` passed.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- Backend `/test` and frontend `/` smoke passed.
- `python scripts/runtime_doctor.py` passed all 67 checks.
- Deployed frontend image:
  `sha256:9608f77af0dfbd8b30026ed60dcb044fad9d2442cc022ced5ee33cc18400be40`.
- Authorized Docker browser acceptance logged in as `admin`, opened the normal
  list at `/view101`, paused the backend, and started a real `querydata`
  request. At 600ms Find and Report both remained enabled; clicking Report
  during the pending query loaded the report dialog after the backend resumed.
  The current Docker seed has no non-row create operation, so the frontend
  metadata test remains the executable proof for the create-command state.
- The backend was unpaused immediately after the browser check, the report
  column request completed normally, `/test` passed, Compose returned to the
  expected state, and all 67 runtime-doctor checks passed again.

## Risks And Follow-ups

- `docs/superpowers/` is unrelated untracked work and remains untouched.
