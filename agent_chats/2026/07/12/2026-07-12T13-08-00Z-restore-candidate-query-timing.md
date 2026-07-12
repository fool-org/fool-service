# Restore Candidate Query Timing

## Prompt

Keep old interaction logic aligned while allowing visual modernization, keep
View metadata ahead of data, and commit each behavior atomically.

## Scope

- Load candidate View metadata when the select-existing picker opens.
- Do not query candidate rows until Find or paging is requested.
- Distinguish not-yet-queried from queried-empty candidate state.
- Keep candidate columns and rows keyed by child-group metadata.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/useChildCandidates.ts`
- `frontend/src/useChildCandidates.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T13-08-00Z-restore-candidate-query-timing.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 151 tests passed, including
  candidate View reset, queried state, View-before-data ordering, and picker
  empty-state text.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including candidate
  `getlistview`, `querydata`, and legacy payload aliases.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailview.js initQueryView(...)` loads `/view`, builds headers, clears rows
  and pagination, writes `记录未知 请查询`, then opens the modal.
- `querylistdata.js querydata()` separately posts `/data/querylist` only when
  the query interaction runs.

## Risks And Follow-Ups

- Authenticated browser acceptance needs a fresh CAPTCHA confirmation.
