# Report Save Form

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare the save-report tab with old `view.jade`.
- Start the report name empty instead of inventing a default business value.
- Restore the old `输入报表信息以保存该报表` heading.
- Let the save form use the available dialog width.
- Preserve the existing metadata-derived save request and route.

## Changed Files

- `frontend/src/ViewReportPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-29-33Z-report-save-form.md`

## Validation

- `cd frontend && npm test -- --run`
  - 145 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - The report chunk remains isolated at 17.28 kB.
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

- Browser acceptance must prove the blank initial value, full-width field,
  save-button gating, and successful save feedback.
