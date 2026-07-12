# Report Condition Group Rendering

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare report condition grouping with old `view.jade` and `mkreport.js`.
- Keep internal `groupPath` ids out of visible page content.
- Represent nested groups with old alternating group colors.
- Restore the old `拆分分组` action copy at each group start.
- Preserve existing recursive filter serialization and metadata binding.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-20-00Z-report-condition-group-rendering.md`

## Validation

- `cd frontend && npm test -- --run`
  - 145 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - The report chunk remains small at 16.53 kB.
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

- Browser acceptance must still prove nested grouping and splitting against a
  loaded report model.
