# PrimeVue View List

## Prompt

- Upgrade the shared View list surface with PrimeVue while preserving
  metadata-driven columns, operations, selection, and backend pagination.

## Scope

- Reimplemented `ListDataTable` with PrimeVue DataTable and dynamic Columns.
- Preserved the shared table props/events used by main, detail candidate, and
  Sudoku list surfaces.
- Upgraded list search, page size, actions, chart tabs, error state, and
  backend-driven pagination controls.
- Updated source contract tests to assert PrimeVue behavior rather than old
  presentation classes and manual previous/next buttons.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `frontend/src/style.css`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T09-50-27Z-primevue-view-list.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 130 tests passed; the Vite production build passed.

## Skipped Checks

- Docker/browser acceptance remains deferred until detail and report controls
  use the same component system.

## Risks And Follow-Ups

- PrimeVue DataTable increases the unsplit application chunk; vendor chunking
  remains a required final-phase check.
