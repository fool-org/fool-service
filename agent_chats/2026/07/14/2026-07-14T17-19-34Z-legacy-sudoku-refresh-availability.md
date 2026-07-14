# Legacy Sudoku Refresh Availability

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits without coupling Vue components to concrete business DTOs.

## Scope

- Compared `Sudoku.jade`, the generated Group include, `groupview.js`, and the
  shared legacy timer path with the Vue Sudoku workflow.
- Removed the Vue-only global request lock from root/group Refresh commands and
  panel auto-refresh.
- Kept map passive refresh, panel timers, request paths, View metadata adapters,
  and rendered panel data unchanged.
- Removed the remaining `disabled` prop chain from App, ViewListPanel, and
  SudokuPanels; added no state, path, DTO binding, or duplicate component.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/useSudokuPanels.ts`
- `frontend/src/ViewListPanel.test.ts`
- `frontend/src/SudokuPanels.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-19-34Z-legacy-sudoku-refresh-availability.md`

## Validation

- `cd frontend && npm test` passed: 18 files, 184 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- The focused `SudokuPanels.test.ts` contract is 16 lines; App, ViewListPanel,
  SudokuPanels, and `useSudokuPanels.ts` all shrank.
- `docker compose build frontend` and frontend replacement passed.
- Deployed frontend image:
  `sha256:2c62bfe876064e73d48b30d422f2cb6d0bc7e0086fdf4d6830bddb3ea0cf5733`.
- `docker compose ps -a` showed backend/frontend/MySQL/Redis running and
  `db-migrate` at `Exited (0)`.
- `python scripts/runtime_doctor.py` passed all 67 checks; backend `/test`
  passed through the doctor.
- Authorized browser acceptance logged in with `admin/admin`, opened
  `/view103`, and confirmed two active Refresh buttons for Orders List and
  Order Group.
- With the backend paused, Orders List Refresh started a real panel request.
  Both buttons retained `disabled=false`, and Order Group Refresh was clicked
  successfully while the first request remained pending.
- After backend resume, both buttons remained enabled, Orders List rendered a
  fresh timestamp, and no error dialog appeared. No database data or metadata
  was changed.

## Risks And Follow-ups

- This parity slice is closed; the broader old-page migration remains active.
- Auto-refresh remains source-contract coverage because seeded Sudoku panels
  have zero refresh interval.
- No screenshot artifact was retained because enabled state and request
  concurrency were verified from the live DOM and restored runtime directly.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
