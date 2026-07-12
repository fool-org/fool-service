# Remove Message Popover

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare `tbar.jade` and `message.js` with the Vue shell message entry.
- Remove the invented bell, manual refresh, message history Popover, and their
  dedicated response/error/pending state.
- Preserve the old 15-second polling behavior, first-message modal, View-first
  detail action, notification badges, avatar, and logout.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-39-35Z-remove-message-popover.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ShellActions` decreased from 12.54 kB to 1.62 kB and transformed modules
  decreased from 284 to 281.
- Source scan confirms no `messageResponse`, `messageItems`, `shellPending`,
  `shellErrorMessage`, message Popover, or Popover-only PrimeVue imports remain.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed, including generated
  message polling, old `getmsg` route, login, and View-first workflows.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated visual inspection requires a fresh CAPTCHA authorization.

## Risks And Follow-Ups

- Final browser acceptance should verify that generated messages open only the
  modal and that the removed bell no longer occupies desktop/mobile topbars.
