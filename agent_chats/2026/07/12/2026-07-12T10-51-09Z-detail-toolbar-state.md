# Detail Toolbar State

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare Edit/Save state with old `detailView.jade` and `detailview.js`.
- Keep Edit and Save mounted for existing editable records.
- Disable Edit during editing and Save outside editing.
- Keep create routes in edit mode with Save only.
- Preserve metadata View operations and their edit-mode guard.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-51-09Z-detail-toolbar-state.md`

## Validation

- `cd frontend && npm test -- --run`
  - 146 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - The detail chunk remains isolated at 70.79 kB.
- `python scripts/check_repo_harness.py` passed.

## Runtime Evidence

- `docker compose build frontend` rebuilt the Vue image successfully.
- `docker compose up -d --no-deps frontend` recreated the frontend at
  `http://localhost:8081`.
- `python scripts/runtime_doctor.py` passed all Compose, auth, View, data,
  detail, child, report, message, chart, and Sudoku checks.
- `docker compose ps -a` shows frontend/backend running, MySQL/Redis healthy,
  and `db-migrate` at `Exited (0)`.

## Skipped Or Downgraded Checks

- Authenticated detail interaction requires a fresh captcha authorization.

## Risks And Follow-Ups

- Browser acceptance must prove stable button positions, disabled states,
  create-route Save-only layout, and operation blocking while editing.
