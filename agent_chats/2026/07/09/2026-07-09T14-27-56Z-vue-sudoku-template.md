# Vue Sudoku Template Shell

# Prompt

Continue the Docker/FoolFrame/Vue migration, keep rendering View-first before
data binding, and avoid concrete business DTO coupling.

# Scope

- Legacy `../FoolFrame/src/Web/views/Sudoku.jade`.
- Legacy child include names from `views/includes/List.jade`, `Group.jade`,
  `Map.jade`, `Item.jade`, and `linechart.jade`.
- Vue View workflow template dispatch only.

# Changes

- Added `viewUsesSudokuTemplate` to detect `TempFile=Sudoku`.
- Added `sudokuPanelKind` to normalize legacy child item `ViewFile` names.
- Rendered a Sudoku panel grid from loaded View item metadata in `App.vue`.
- Kept child-panel data loading out of this slice so child panels do not bind
  to the root View's `querydata` rows or any concrete business DTO fields.
- Updated parity notes and task state.

# Validation

- Red:
  `cd frontend && npm test -- viewWorkflow.test.ts`
  failed because `viewUsesSudokuTemplate` and `sudokuPanelKind` did not exist.
- Red:
  `cd frontend && npm test -- payload.test.ts`
  failed because `App.vue` did not render the Sudoku template branch.
- Green:
  `cd frontend && npm test -- viewWorkflow.test.ts`
  passed `43` tests.
- Green:
  `cd frontend && npm test -- payload.test.ts`
  passed `63` tests.
- Green:
  `cd frontend && npm test`
  passed `116` tests.
- Green:
  `cd frontend && npm run build`.
- Green:
  `python scripts/check_repo_harness.py`.
- Green:
  `git diff --check`.
- Green:
  `docker compose up -d --build frontend`, followed by
  `docker compose up -d --no-deps --force-recreate frontend`.
- Green:
  `python scripts/runtime_doctor.py`.

# Risks

- The Sudoku panel shell does not yet issue independent `querydata` calls for
  each child panel `ListViewId`. That is deliberate to avoid incorrectly
  reusing root View rows as child-panel data.

# Follow-ups

- Add child-panel `getlistview(ListViewId)` then `querydata(ListViewId)` loading
  for Sudoku panels, reusing the existing View-first workflow helpers.
