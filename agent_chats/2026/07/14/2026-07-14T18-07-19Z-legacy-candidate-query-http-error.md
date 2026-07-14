# Legacy Candidate Query HTTP Error

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `detailView.jade`'s `#selectdialog` through its mounted
  `QuerylistdataController` before comparing the candidate data path.
- Reused the existing optional `silentTransport` action policy only for the
  candidate `child-select-data` request.
- Kept candidate View metadata loading and response-backed business-error
  presentation unchanged.
- Added no component, API route, DTO binding, or abstraction.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-07-19Z-legacy-candidate-query-http-error.md`

## Validation

- `cd frontend && npm test -- --run payload.test.ts` passed: 1 file, 83 tests.
- `cd frontend && npm test` passed: 19 files, 187 tests.
- `cd frontend && npm run build` passed, including `vue-tsc --noEmit`.
- `python scripts/check_repo_harness.py` passed.
- Compose built and replaced the frontend with image
  `sha256:7179af2aed562a4959a0529e1dfda3cd796b246b3d218501f28dd7ab59a99869`;
  the running container references the same image.
- Authorized browser acceptance opened `/view100/1001` and the `选择 Items`
  dialog. A successful baseline query rendered four candidate rows, four
  Select commands, `共4条记录`, and the fixed ten-row table from the candidate
  View metadata.
- With the backend stopped, Nginx recorded settled `querydata` `502` / `504`
  responses. The dialog remained open with all four candidate rows, record
  count, paging structure, and enabled Find; no `HTTP 502`, `HTTP 504`,
  `Failed to fetch`, or shared `发生错误` message appeared.
- After backend restart, Find succeeded in the same authenticated dialog and
  retained the four-row result with no error residue.
- `docker compose ps -a` passed: backend/frontend/MySQL/Redis were running,
  MySQL/Redis were healthy, and `db-migrate` was `Exited (0)`.
- Runtime restoration checks passed: `SW_SYS_VIEW` View 100 remained file
  `990001` with refresh interval `0`, `fool_sys_view` View 100 remained at
  interval `0`, and `market_order` / `market_order_item` counts remained 8/4.
- `python scripts/runtime_doctor.py` passed all 67 checks.

## Risks And Follow-ups

- Candidate View metadata-load transport behavior is intentionally outside this
  slice because old `initQueryView` owns a separate loading-dialog lifecycle.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
