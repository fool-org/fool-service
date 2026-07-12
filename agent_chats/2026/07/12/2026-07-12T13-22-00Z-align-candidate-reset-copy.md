# Align Candidate Reset Copy

## Prompt

Keep old interaction logic aligned while allowing visual modernization, keep
View metadata ahead of data, and commit each behavior atomically.

## Scope

- Use the runtime candidate reset text written by `initQueryView`.
- Correct current task and migration evidence that cited the pre-JavaScript
  Jade placeholder instead.
- Leave candidate query, paging, selection, and save behavior unchanged.

## Changed Files

- `frontend/src/useChildCandidates.ts`
- `frontend/src/useChildCandidates.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-22-00Z-align-candidate-reset-copy.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 152 tests passed, including the
  exact runtime reset copy assertion.
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

- `detailView.jade` initially contains `记录数未知,请查询`.
- Before showing the picker, `detailview.js initQueryView(...)` replaces that
  placeholder with runtime text `记录未知 请查询`, clears result rows, and clears
  pagination. This JavaScript-written state is the visible source of truth.

## Risks And Follow-Ups

- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.
