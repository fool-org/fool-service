# Legacy List Row Fill Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files controlled and reuse code.

## Scope

- Compare old fixed page-row rendering across list templates and Sudoku
  partials with the shared Vue table.
- Restore inert filler rows without mutating View data or business DTOs.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/ViewListPanel.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-10-53Z-legacy-list-row-fill.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js` lines 162-165
  append blank rows until each result reaches the controller's page size.
- `view.jade`, `viewWithChart.jade`, and the candidate controller configure a
  page size of 10.
- `views/includes/List.jade` configures Sudoku list partials with size 5 and
  preallocates five data rows below the header.

## Implementation

- The 110-line shared `ListDataTable.vue` accepts generic `minimumRows` and
  appends symbol-marked presentation rows only when returned data is short.
- Filler rows render nonbreaking cells, receive the existing stripe treatment,
  expose no buttons, and are not passed to selection events.
- Main/chart and candidate callers pass their existing page size; both Sudoku
  list callers pass the old partial size of five.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:3eae04f73cf720f94f67cf46562f8daa989042a3367fb9f2a396c6c43cbd458d`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100`: eight data rows plus two fillers produced ten body
  rows; only the eight real rows exposed their 16 metadata operations.
- Authenticated `/view100/1001`: one `Legacy` candidate plus nine fillers
  produced ten rows and exactly one Select action.
- Authenticated `/view103`: both rendered Sudoku list tables had five rows and
  all five root panels retained the one-time 458px equal-height lock.
- At 390px, main and candidate pages retained fixed rows with page
  `scrollWidth` equal to 390; the candidate dialog stayed 358px wide.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-list-row-fill/main-desktop.jpg`
- `artifacts/runs/20260713-legacy-list-row-fill/main-mobile.jpg`
- `artifacts/runs/20260713-legacy-list-row-fill/candidate-desktop.jpg`
- `artifacts/runs/20260713-legacy-list-row-fill/candidate-mobile.jpg`
- `artifacts/runs/20260713-legacy-list-row-fill/sudoku-desktop.jpg`

## Risks And Follow-Ups

- Filler rows are presentation-only and intentionally omitted from the source
  `rows` prop, paging totals, selection callbacks, and operation rendering.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
