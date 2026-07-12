# Shell Text Brand

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with old
  FoolFrame, with every behavior change committed atomically.

## Scope

- Compare the signed-in header with old `default.jade`.
- Remove the invented app-initial tile and computed mark.
- Render App name/version inline in the shell and mobile Drawer.
- Reduce the desktop shell header from 72px to the old 50px navbar scale.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T11-01-35Z-shell-text-brand.md`

## Validation

- `cd frontend && npm test -- --run`: 9 files, 147 tests passed.
- `cd frontend && npm run build`: TypeScript and Vite production build passed;
  `ViewDetailPanel` remained 70.31 kB.
- `docker compose build frontend`: passed.
- `python scripts/runtime_doctor.py`: all checks passed.
- `python scripts/check_repo_harness.py`: passed.
- `git diff --check`: passed.

## Runtime Evidence

- Rebuilt frontend image deployed at `http://localhost:8081`.
- `docker compose ps -a`: backend/frontend up, MySQL/Redis healthy, and
  `db-migrate` exited successfully with code 0.

## Skipped Or Downgraded Checks

- Authenticated shell visual inspection requires a fresh CAPTCHA authorization.

## Risks And Follow-Ups

- Browser acceptance still needs long App names, desktop navigation and user
  actions, plus the mobile Drawer checked with authenticated metadata.
