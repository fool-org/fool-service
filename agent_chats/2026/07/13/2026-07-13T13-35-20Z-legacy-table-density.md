# Legacy Table Density Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the 5px condensed density used by normal and chart tables.
- Restore the 8px default density used by candidate and Sudoku tables.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-35-20Z-legacy-table-density.md`

## Legacy Evidence

- `view.jade` and `viewWithChart.jade` apply `.table-condensed`; Bootstrap 3
  defines that cell padding as 5px.
- `detailView.jade` candidate results and `views/includes/List.jade` Sudoku
  lists omit `.table-condensed`; Bootstrap's default table padding is 8px.
- The deployed shared PrimeVue table previously applied its small size
  everywhere and computed to 5.625px vertical by 7.5px horizontal padding.

## Implementation

- Added a default-true `condensed` prop to the 120-line shared table and mapped
  it to PrimeVue's existing size option.
- Candidate and the two Sudoku call sites opt out with one prop each; no table
  markup, request, or state was duplicated.
- Shared CSS now defines Bootstrap's exact 8px default and 5px condensed
  padding for both headers and cells.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image id
  `sha256:de4b7cb8bb8b81fadcf19df5564e5d00a9313604a01642fc79873c3f0f674b91`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` headers/cells measured 5px on every side.
- Authenticated `/view102/1001` candidate query rendered ten rows and measured
  8px on every side; Cancel closed it without changing data.
- Authenticated `/view103` headers/cells measured 8px; at 390px the same
  density remained and page `scrollWidth` stayed 390.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-table-density/normal-desktop.jpg`
- `artifacts/runs/20260713-legacy-table-density/candidate-desktop.jpg`
- `artifacts/runs/20260713-legacy-table-density/sudoku-desktop.jpg`
- `artifacts/runs/20260713-legacy-table-density/sudoku-mobile.jpg`

## Risks And Follow-Ups

- Row action buttons retain PrimeVue's small button size, matching the old
  `btn-group-sm`; this change only restores table-cell density.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
