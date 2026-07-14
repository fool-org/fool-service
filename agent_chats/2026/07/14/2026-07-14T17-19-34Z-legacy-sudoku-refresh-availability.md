# Legacy Sudoku Refresh Availability

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits without coupling Vue components to concrete business DTOs.

## Scope

- Compared `Sudoku.jade`, the generated Group include, `groupview.js`, and the
  shared legacy timer path with the Vue Sudoku workflow.
- Removed the Vue-only global request lock from root/group Refresh commands and
  panel auto-refresh.
- Kept map passive refresh, panel timers, request paths, View metadata adapters,
  and rendered panel data unchanged.
- Removed the remaining `disabled` prop chain from App, ViewListPanel, and
  SudokuPanels; added no state, path, DTO binding, or duplicate component.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/useSudokuPanels.ts`
- `frontend/src/ViewListPanel.test.ts`
- `frontend/src/SudokuPanels.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T17-19-34Z-legacy-sudoku-refresh-availability.md`

## Validation

- `cd frontend && npm test` passed: 18 files, 184 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- The focused `SudokuPanels.test.ts` contract is 16 lines; App, ViewListPanel,
  SudokuPanels, and `useSudokuPanels.ts` all shrank.
- Pending Docker rebuild, runtime doctor, and authorized browser acceptance.

## Risks And Follow-ups

- Browser-verify both visible Refresh commands during a real paused-backend
  panel request and restore Compose afterward.
- Auto-refresh remains source-contract coverage because seeded Sudoku panels
  have zero refresh interval.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
