# Child Picker Protocol Controls

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare select-existing child controls with old `detailView.jade`.
- Remove editable page and page-size protocol inputs.
- Keep the old query field and search command.
- Keep page size fixed at 10 and page movement on previous/next commands.
- Delete unused component events, App bindings, and composable mutators.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/useChildCandidates.ts`
- `frontend/src/useChildCandidates.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-53-21Z-child-picker-protocol-controls.md`

## Validation

- `cd frontend && npm test -- --run`
  - 146 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - The detail chunk decreased from 70.79 kB to 70.31 kB.
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

- Authenticated child-picker interaction requires a fresh captcha authorization.

## Risks And Follow-Ups

- Browser acceptance must prove query reset, fixed request size, paging bounds,
  selection, and dialog close behavior.
