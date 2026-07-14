# Sudoku Panel Transport Error

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced `Sudoku.jade` panel templates before their data controllers.
- Confirmed List/Group use `querylistdata.js` / `groupview.js`, while Map, Item,
  and line chart use the success-only `ServerUtil.js` request helper.
- Added one optional shared action-policy parameter to panel View/Data loaders.
- Reused the existing `silentTransport` policy only inside `useSudokuPanels`.
- Kept response-backed business errors and non-Sudoku callers unchanged.

## Changed Files

- `frontend/src/useViewDataWorkflow.ts`
- `frontend/src/useSudokuPanels.ts`
- `frontend/src/useViewDataWorkflow.test.ts`
- `frontend/src/SudokuPanels.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T18-17-44Z-sudoku-panel-transport-error.md`

## Validation

- `cd frontend && npm test -- --run useViewDataWorkflow.test.ts
  SudokuPanels.test.ts` passed: 2 files, 10 tests.
- `cd frontend && npm test` passed: 19 files, 189 tests. The first full run
  found one stale three-argument source assertion; updating it to the new
  four-argument policy call restored the green suite.
- `cd frontend && npm run build` passed, including `vue-tsc --noEmit`.
- `python scripts/check_repo_harness.py` passed.
- Compose built and replaced the frontend with image
  `sha256:c3280b496c00c46f4a65400c09e87b06a04de513b06320d675a8f43a7a6341a1`;
  the running container references the same image.
- Authorized browser acceptance loaded `/view103` with Orders List and Group
  Orders five-row tables, Price Chart, Customer Map, and Order Item content;
  two list Refresh commands were available.
- With the backend stopped, the first List Refresh settled as a Nginx
  `getlistview` `502`. All loaded panel content and both Refresh commands
  remained, with no `HTTP 502`, `Failed to fetch`, or shared `发生错误` message.
- After backend restart, the same Refresh succeeded and advanced the List
  update time from `18:21:12` to `18:22:16` without reloading the route.
- `docker compose ps -a` passed: backend/frontend/MySQL/Redis were running,
  MySQL/Redis were healthy, and `db-migrate` was `Exited (0)`.
- Runtime restoration checks passed: `SW_SYS_VIEW` View 100 remained file
  `990001` with refresh interval `0`, `fool_sys_view` View 100 remained at
  interval `0`, and `market_order` / `market_order_item` counts remained 8/4.
- `python scripts/runtime_doctor.py` passed all 67 checks.

## Risks And Follow-ups

- Candidate View metadata-load transport behavior remains a separate decision
  because old `initQueryView` leaves its loading dialog open indefinitely.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
