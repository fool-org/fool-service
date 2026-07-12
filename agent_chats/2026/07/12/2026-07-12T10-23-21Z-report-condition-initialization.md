# Report Condition Initialization

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare condition-row creation with old `mkreport.js`.
- Add conditions without silently selecting the first View field.
- Leave comparison and value controls uninitialized until metadata drives
  them from an explicit field choice.
- Keep the first relation cell empty and restore `与/或` display labels while
  preserving `and/or` request values.
- Localize condition editor accessibility labels.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-23-21Z-report-condition-initialization.md`

## Validation

- `cd frontend && npm test -- --run`
  - 145 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - The report chunk decreased from 16.53 kB to 16.51 kB.
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

- Browser acceptance must prove the blank initial row, metadata-driven editor,
  condition grouping, and generated request against the loaded report model.
