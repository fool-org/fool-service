# Restore Candidate Record Feedback

## Prompt

Keep old interaction logic aligned while allowing visual modernization, keep
View metadata ahead of data, and commit each behavior atomically.

## Scope

- Restore the old candidate record-count text before and after querying.
- Keep zero results distinct from the not-yet-queried state.
- Place result status and paging after the candidate table like the old Jade.

## Changed Files

- `frontend/src/useChildCandidates.ts`
- `frontend/src/useChildCandidates.test.ts`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-18-00Z-restore-candidate-record-feedback.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 152 tests passed, including
  exact unknown, zero-result, and nonzero candidate record-count text.
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

- `detailView.jade` initializes candidate `#info` as `记录数未知,请查询` below
  `#datalist` and before `#pagenav`.
- `querylistdata.js` passes query totals to `navbar.updateNavbar(...)`.
- `navbar.js updateInfo()` changes `#info` to exact `共N条记录` and rebuilds
  paging after every successful query.

## Risks And Follow-Ups

- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.
