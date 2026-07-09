# Vue Sudoku Item Panels

## Prompt

- Continue the FoolFrame migration goal with Docker runtime kept healthy,
  Vue frontend parity, View-first rendering, code reuse, and atomic commits.

## Scope

- Compared `../FoolFrame/src/Web/views/includes/Item.jade` and
  `../FoolFrame/src/Web/public/javascripts/app/subitem.js`.
- Added the smallest Vue parity slice for Sudoku `Item` panels.
- Reused the existing Sudoku child `ListViewId` load path and shared row item
  helpers.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/09/2026-07-09T15-02-05Z-vue-sudoku-item-panels.md`

## Behavior

- `legacyItemFields` renders the first loaded child row's legacy `Items` as
  label/value pairs.
- Sudoku `Item` panels render those fields as a compact table.
- Raw `row.values` DTO maps are ignored.
- Sudoku `Group` remains the only unsupported Sudoku include in this template
  sequence.

## Red Check

- `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
  - Failed before implementation because `legacyItemFields` did not exist and
    the Vue Sudoku template had no `item` branch.

## Validation

- `cd frontend && npm test -- viewWorkflow.test.ts payload.test.ts`
- `cd frontend && npm test`
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `docker compose up -d --build frontend`
- `docker compose up -d --no-deps --force-recreate frontend && docker compose ps && python scripts/runtime_doctor.py`

## Risks

- This slice uses the already loaded child `querydata` row `Items` rather than
  adding a separate `querydatadetail` panel call. That keeps rendering
  View-first and useful without adding another request path.

## Follow-ups

- Implement Sudoku `Group` only after mapping the legacy tab behavior and
  deciding whether nested child views should load eagerly or lazily.
