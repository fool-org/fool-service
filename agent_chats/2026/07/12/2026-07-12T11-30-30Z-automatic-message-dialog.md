# Automatic Message Dialog

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare old `message.js`, `showerror.js`, and `default.jade` message delivery
  with the Vue shell.
- Open the first item from each non-empty 15-second `getmsg` poll immediately.
- Reuse existing message field adapters and View-first detail navigation.
- Keep the responsive topbar message list as a history entry.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-30-30Z-automatic-message-dialog.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ShellActions` is 12.43 kB.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed, including non-empty
  `getmsg`, legacy route, login, and View/data workflows.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated visual inspection requires a fresh CAPTCHA authorization; the
  frontend state transition and deployed message API were verified separately.

## Risks And Follow-Ups

- Final browser acceptance should verify automatic dialog opening, dismissal,
  and View/detail navigation at desktop and mobile widths.
