# Vue Sudoku Group Panels

## Prompt

- Continue the Docker/Vue FoolFrame migration with View-first rendering,
  maximum reuse, controlled file size, and atomic commits.

## Scope

- Compared `../FoolFrame/src/Web/views/includes/Group.jade` and
  `../FoolFrame/src/Web/public/javascripts/app/groupview.js`.
- Added one-level Sudoku `Group` panel parity.
- Extracted Sudoku panel rendering from `App.vue` into `SudokuPanels.vue` to
  keep the root component under the harness line budget.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T15-10-48Z-vue-sudoku-group-panels.md`

## Behavior

- Sudoku `Group` panels render child tabs from the loaded group View `Items`.
- Child tabs with `ListViewType=0` load their own `ListViewId` through the
  existing `getlistview` -> `querydata` helper path.
- Child tabs with `ListViewType=1` render as explicit simple-item placeholders,
  matching the old `groupview.js` behavior.
- No recursive group loader or new data protocol was added.

## Red Check

- `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
  - Failed before implementation because `sudokuPanelListViewType` did not
    exist and the Vue Sudoku template had no group child-list branch.

## Validation

- `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
- `cd frontend && npm test`
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build frontend`
- `docker compose up -d --no-deps --force-recreate frontend && docker compose ps && python scripts/runtime_doctor.py`

## Risks

- Group loading is intentionally one-level, matching the legacy controller
  behavior inspected here. Recursive group expansion remains out of scope until
  a real legacy configuration needs it.

## Follow-ups

- Add a runtime-doctor seed for a real `Group` panel if Docker fixture coverage
  needs to prove nested group list rendering outside frontend tests.
