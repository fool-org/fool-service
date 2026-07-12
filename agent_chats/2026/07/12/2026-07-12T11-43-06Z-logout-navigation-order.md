# Logout Navigation Order

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare `tbar.jade` navigation order with the Vue desktop header.
- Move `安全退出` after the metadata menu and out of the right user area.
- Add the same final navigation action to the mobile Drawer.
- Preserve logout behavior, pending-state disabling, avatar, user name, and
  automatic system-message dialog behavior.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-43-06Z-logout-navigation-order.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ShellActions` decreased from 1.59 kB to 1.41 kB.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed, including logout,
  login restoration, messages, and View-first workflows.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated desktop/mobile visual inspection requires a fresh CAPTCHA
  authorization.

## Risks And Follow-Ups

- Final browser acceptance should verify menu order and logout visibility at
  desktop and mobile widths.
