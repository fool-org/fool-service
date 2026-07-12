# Restore Candidate View Loading

## Prompt

Keep old interaction logic aligned while allowing visual modernization, keep
View metadata ahead of data, and commit each behavior atomically.

## Scope

- Show the old candidate View loading title and message.
- Keep that loading modal non-dismissible.
- Await candidate View metadata before opening the picker.
- Leave the picker closed when metadata loading fails.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-11-00Z-restore-candidate-view-loading.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 151 tests passed, including the
  awaited metadata loader, loading modal content, and prop-based picker opening.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including candidate
  View metadata and data routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailview.js initQueryView(...)` opens `#loading-dialog` with `加载中` and
  `正在加载，请稍后....`, loads `/view`, hides loading, then invokes the callback.
- The add callback opens `#selectdialog`, so the picker cannot precede metadata.

## Risks And Follow-Ups

- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.
