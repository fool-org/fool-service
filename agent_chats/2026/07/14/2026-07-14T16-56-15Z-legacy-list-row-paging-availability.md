# Legacy List Row And Paging Availability

## Prompt

Continue aligning the old page layout, style, and interactions in atomic
commits without coupling Vue components to concrete business DTOs.

## Scope

- Compared `view.jade`, `querylistdata.js`, and `navbar.js` row and pagination
  commands with the Vue main list.
- Removed the Vue-only global request lock from main-list row navigation and
  the shared legacy paginator.
- Kept page-boundary behavior and Sudoku table/refresh pending guards.
- Made the shared table's existing disabled prop default to false and removed
  redundant false props from the already-active detail candidate controls.
- Added no state, request path, route, DTO binding, or duplicate component.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/LegacyPagination.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `frontend/src/ViewListPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T16-56-15Z-legacy-list-row-paging-availability.md`

## Validation

- `cd frontend && npm test` passed: 16 files, 182 tests.
- `cd frontend && npm run build` passed.
- `python scripts/check_repo_harness.py` passed.
- The focused `ViewListPanel.test.ts` contract is 24 lines;
  `LegacyPagination.vue` shrank to 40 lines and `ViewDetailPanel.vue` to 459.
- Pending Docker rebuild, runtime doctor, and authorized browser acceptance.

## Risks And Follow-ups

- Browser-verify row navigation and a non-boundary page link during an
  unrelated real request without changing persistent business data.
- Remove any temporary local acceptance rows and verify the original order
  count afterward.
- `docs/superpowers/` is unrelated untracked work and remains untouched.
