# Text-only Logout

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare `tbar.jade` safe logout with the Vue shell command.
- Remove the invented sign-out icon.
- Preserve logout behavior, disabled state, and signed-out flow.

## Changed Files

- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-40-59Z-text-only-logout.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ShellActions` decreased from 1.62 kB to 1.59 kB.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed, including logout,
  login, messages, and View-first workflows.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated visual inspection requires a fresh CAPTCHA authorization.

## Risks And Follow-Ups

- Final browser acceptance should verify the text-only logout control at
  desktop and mobile widths.
