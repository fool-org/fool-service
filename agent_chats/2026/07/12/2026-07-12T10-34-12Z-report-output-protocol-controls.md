# Report Output Protocol Controls

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare output-tab controls with old `view.jade` and `mkreport.js`.
- Keep report page size at the old protocol-only value of 10.
- Remove the invented page-size editor and manual metadata reload command.
- Preserve ViewId-bound report metadata loading when the dialog mounts.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-34-12Z-report-output-protocol-controls.md`

## Validation

- `cd frontend && npm test -- --run`
  - 145 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - The report chunk decreased from 17.21 kB to 16.81 kB.
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

- Browser acceptance must prove the output tab has only the old three-list
  selector and that paging requests still use ten rows.
