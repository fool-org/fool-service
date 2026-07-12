# List Toolbar Layout

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Compare the main list toolbar with old `view.jade`.
- Remove the invented visible query label while retaining an accessible name.
- Restore compact right-aligned desktop controls.
- Let only the query input expand to a full row on narrow screens.
- Preserve search, report, and View-metadata create operations.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T10-36-33Z-list-toolbar-layout.md`

## Validation

- `cd frontend && npm test -- --run`
  - 145 tests passed across 9 files.
- `cd frontend && npm run build`
  - Vue TypeScript checking and Vite production build passed.
  - The View list chunk remains isolated at 18.04 kB.
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

- Authenticated list interaction requires a fresh captcha authorization.

## Risks And Follow-Ups

- Browser acceptance must prove right alignment, mobile wrapping, Enter search,
  report opening, and metadata create navigation.
