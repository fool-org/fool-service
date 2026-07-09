# Vue Sudoku Child Panels

# Prompt

Continue the Docker/FoolFrame/Vue migration. Keep View rendering before data
loading, avoid concrete business DTO binding, and keep file size/reuse under
control.

# Scope

- Vue `TempFile=Sudoku` flow.
- Child panel `ListViewId` loading.
- Existing `useViewDataWorkflow` View-first helper.

# Changes

- Added `loadViewDataById` to reuse the existing
  `getlistview(ViewId)` -> `querydata(ViewId)` path for child panels.
- Made Sudoku current-data refresh load panel data and skip root View
  `querydata`.
- Rendered list-type Sudoku panel rows through `ListDataTable` with child View
  columns.
- Updated parity notes, task state, and delivery evidence.

# Validation

- Red:
  `cd frontend && npm test -- payload.test.ts`
  failed because Sudoku child loading still required root querydata.
- Red:
  `cd frontend && npm test -- payload.test.ts useViewDataWorkflow.test.ts`
  failed after the first code pass because the source-slice assertion matched
  the `onMounted` import.
- Green:
  `cd frontend && npm test -- payload.test.ts useViewDataWorkflow.test.ts`
  passed `69` tests.
- Green:
  `cd frontend && npm test`
  passed `118` tests.
- Green:
  `cd frontend && npm run build`.
- Green:
  `python scripts/check_repo_harness.py`.
- Green:
  `git diff --check`.
- Green:
  `docker compose up -d --build frontend`; compose also rebuilt backend with
  `mvn -DskipTests package` and the Maven reactor ended `BUILD SUCCESS`.
- Green:
  `docker compose up -d --no-deps --force-recreate frontend && docker compose ps`.
- Green:
  `python scripts/runtime_doctor.py`.

# Risks

- Runtime doctor proves the stable View/data runtime and rebuilt frontend
  proxy, but it does not browser-click a seeded Sudoku page. The regression
  guard for the Sudoku-specific root-query skip is in frontend tests.

# Follow-ups

- Continue converting any remaining FoolFrame template variants with the same
  View-first pattern: render metadata first, then load data from the rendered
  View id.
