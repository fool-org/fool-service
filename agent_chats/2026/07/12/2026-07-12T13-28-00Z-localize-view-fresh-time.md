# Localize View Fresh Time

## Prompt

Keep old interaction logic aligned while allowing visual modernization, reuse
shared View helpers, and commit each behavior atomically.

## Scope

- Format list `FreshTime` through the browser locale like old query pages.
- Support the current ISO response and legacy `/Date(...)/` response shape.
- Reuse one helper for main and Sudoku View status rows.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-28-00Z-localize-view-fresh-time.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 152 tests passed, including ISO,
  legacy Date, and unparseable FreshTime cases.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including main and
  Sudoku View/data routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `querylistdata.js` converts response `FreshTime` to a JavaScript Date and
  calls `toLocaleString()` before rendering `更新时间`.
- The migrated backend emits ISO `LocalDateTime`; old Web payloads used the
  `/Date(...)/` shape.
- Main and Sudoku Vue panels already share `listFreshTime`, so the conversion
  belongs at that View-response boundary.

## Risks And Follow-Ups

- Locale output intentionally follows the user's browser and is not a fixed
  server-side format.
