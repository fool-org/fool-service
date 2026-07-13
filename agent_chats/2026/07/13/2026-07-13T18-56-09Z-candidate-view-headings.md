# Candidate View Headings

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep candidate rendering driven by loaded View
metadata and reuse the shared table.

## Scope

- Remove the Vue-only operation heading from the select-existing candidate
  table.
- Preserve row-level Select actions and all existing query/paging behavior.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T18-56-09Z-candidate-view-headings.md`

## Legacy Evidence

- `detailview.js initQueryView` clears the candidate table and appends one
  heading only for each loaded `data.view.data.Items` entry.
- `querylistdata.js` adds the row-level Select action after data arrives but
  does not append an operation heading when View headings already exist.

## Implementation

- Added a default-on `showActionHeader` presentation option to the shared
  `ListDataTable` component.
- Disabled only that heading in the detail candidate picker; main View and
  Sudoku tables retain their existing operation heading.
- Kept the action column and Select button intact. No duplicate candidate table,
  data helper, DTO branch, or dependency was added.

## Validation

- Focused payload contract: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend-only Docker build and forced recreation passed. Deployed image id:
  `sha256:2d1a2f7795137561bc36010a11c9596bbfe1fbfbee452d2689df47984909a5c4`.
- `python scripts/check_repo_harness.py`, `git diff --check`, backend `/test`,
  frontend `/`, and Compose service-state checks passed.
- Authenticated `/view102/1001` browser proof showed only `Item ID` and
  `Item Name` headings before query. After Find, the operation-heading count
  remained zero while four Select buttons rendered.
- Selecting candidate `2002 Existing fee` closed the dialog and staged one
  local row; reloading removed the unsaved row and restored the three persisted
  child rows.

## Runtime Evidence

- `artifacts/runs/20260714-candidate-view-headings/candidate-results.png`

## Risks And Follow-Ups

- The trailing action column intentionally has no heading to match old Web's
  table shape; its buttons retain accessible Select labels.
- The CAPTCHA-backed runtime doctor was not run because the existing
  authenticated browser session covered this frontend-only interaction and no
  fresh CAPTCHA was generated.
