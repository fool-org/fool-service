# Restore Candidate Query Page

## Prompt

Keep old interaction logic aligned while allowing visual modernization, keep
View metadata ahead of data, and commit each behavior atomically.

## Scope

- Reset candidate Find requests to page 1.
- Preserve the requested page for previous/next candidate navigation.
- Leave candidate request DTOs, filters, and result rendering unchanged.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-19-00Z-restore-candidate-query-page.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 152 tests passed, including
  separate Find-reset and paging-preserve source assertions.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including candidate
  View metadata/data and parent/child save routes.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `querylistdata.js query()` sets `$scope.page = 1` before `querydata()`.
- `navbar.navToPage(...)` sets the requested page and calls `querydata()`
  directly, so paging does not pass through the page-1 reset.

## Risks And Follow-Ups

- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.
