# List Unsupported Template State

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Trace unknown `TempFile` handling through App and ViewListPanel.
- Keep the existing no-query guard for unsupported templates.
- Remove the duplicate App-level error so only one warning renders.
- Localize the warning while preserving the metadata template name.
- Replace the transient English no-View title with `加载视图`.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-43-41Z-list-unsupported-template-state.md`

## Validation

- `cd frontend && npm test -- --run`
  - 145 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - The View list chunk decreased from 18.04 kB to 18.03 kB.
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

- Authenticated unsupported-template interaction requires a fresh captcha and
  a runtime View configured with an unknown `TempFile`.

## Risks And Follow-Ups

- Browser acceptance must prove a supported View has no warning and an unknown
  template renders exactly one warning without issuing `querydata`.
