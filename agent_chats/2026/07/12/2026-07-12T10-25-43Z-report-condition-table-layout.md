# Report Condition Table Layout

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare the condition tab with old `view.jade` and `mkreport.js`.
- Restore icon-only top add and group commands.
- Restore visible `与/或`, `字段`, `运算`, and `值` headers.
- Put delete, selection, and group controls before relation and field values.
- Restore the bottom add-condition command.
- Keep the dense editor at a stable, horizontally scrollable mobile width.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-25-43Z-report-condition-table-layout.md`

## Validation

- `cd frontend && npm test -- --run`
  - 145 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - The report chunk remains isolated at 17.27 kB.
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

- Authenticated report interaction requires a fresh captcha authorization.

## Risks And Follow-Ups

- Browser acceptance must prove desktop column alignment, narrow-screen
  scrolling, add/delete/group controls, and request generation.
