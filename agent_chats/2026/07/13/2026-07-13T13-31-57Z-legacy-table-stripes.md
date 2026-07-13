# Legacy Table Stripe Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore Bootstrap's stripe color and first-row phase for list tables.
- Remove invented striping from Sudoku List and Group List panels.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-31-57Z-legacy-table-stripes.md`

## Legacy Evidence

- `view.jade`, `viewWithChart.jade`, and `detailView.jade` apply both
  `.table-striped` and `.table-hover` to list/candidate tables.
- `views/includes/List.jade` applies only `.table-hover` to Sudoku lists.
- Bootstrap 3 styles odd DOM rows with `#f9f9f9`; the deployed PrimeVue table
  instead styled the second row `rgb(248, 250, 252)` and striped Sudoku too.

## Implementation

- Added a default-true `striped` prop to the 118-line shared table and bound it
  to PrimeVue's existing `stripedRows` option.
- Disabled the prop at the two Sudoku list call sites; no duplicate table or
  data state was introduced.
- Overrode PrimeVue's row-index classes so the first row uses exact Bootstrap
  `#f9f9f9` and the second row is white.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image id
  `sha256:b06f8aba1eeeccbc9b29b22e2f16ccc2957ef35a4cb4affd71fa6bcaf17fb2da`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100`: rows alternated `249/255/249/255` RGB and retained
  `p-datatable-striped`.
- Authenticated `/view103`: both Sudoku tables omitted the striped class and
  rendered all inspected rows white; at 390px page `scrollWidth` stayed 390.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-table-stripes/normal-desktop.jpg`
- `artifacts/runs/20260713-legacy-table-stripes/sudoku-desktop.jpg`
- `artifacts/runs/20260713-legacy-table-stripes/sudoku-mobile.jpg`

## Risks And Follow-Ups

- Contextual `RowFmt` and hover cell backgrounds remain more specific and keep
  their legacy colors over the row stripe.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
