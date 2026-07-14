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
- Pending Compose build and authorized `/view103` browser acceptance.

## Risks And Follow-ups

- Browser acceptance must load Sudoku content, stop the backend, click a visible
  List Refresh, and prove existing panel content remains without shared error;
  then restart the backend and prove Refresh recovers.
- Candidate View metadata-load transport behavior remains a separate decision
  because old `initQueryView` leaves its loading dialog open indefinitely.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
