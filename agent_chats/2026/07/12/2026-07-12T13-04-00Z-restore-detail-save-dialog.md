# Restore Detail Save Dialog

## Prompt

Keep old interaction logic aligned while allowing visual modernization and
commit each behavior atomically.

## Scope

- Show the old Save loading title and message for parent save requests.
- Keep the loading modal non-dismissible.
- Return only after a successful save closes the modal.
- Close without navigation on failure so the error remains actionable.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-04-00Z-restore-detail-save-dialog.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 151 tests passed, including
  loading-dialog content, save-state wiring, and post-hide navigation source.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including existing/new
  parent save surfaces and surrounding detail routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailview.js beginsave()` sets `保存中` / `正在保存，请稍后....`, opens
  `#loading-dialog`, and registers `history.go(-1)` on modal hidden.
- Both old new-object and existing-object success callbacks hide that modal.

## Risks And Follow-Ups

- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.
