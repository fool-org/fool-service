# Legacy Sudoku Empty Item Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Inspect the old Sudoku Item partial and controller
before changing the Vue empty-field rendering.

## Scope

- Keep the six-row Sudoku Item table when detail SimpleData is empty.
- Preserve View-first detail loading and the existing shared Item renderer.

## Changed Files

- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T16-55-17Z-legacy-sudoku-item-empty.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/includes/Item.jade` always renders the Item panel
  shell and starts `GetItemController`.
- `../FoolFrame/src/Web/public/javascripts/app/subitem.js` always appends its
  table and pads the matrix to six rows, including when `SimpleData` is empty.

## Implementation

- Removed only the field-count condition from the existing Sudoku
  `LegacyItemPanel` branch.
- Reused `LegacyItemPanel`'s existing two-fields-per-row and minimum-six-row
  logic; added no duplicated View, detail, or table state.
- Added no DTO, API, component, dependency, or CSS changes.

## Validation

- Full frontend suite: 155 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced no-dependency recreation passed.
  Deployed image id:
  `sha256:29f3fc75efb913947a808b9fc5437800e1ffc9bf87ade5b61789acc5c8f26b88`.
- Authenticated `/view103` rendered one `Order Item` table with six rows: three
  View-derived field rows and three filler rows. The adjacent Group panel and
  its tabs remained available.
- Stable focused screenshots at 1440x1000 and 390x844 show the Item matrix
  without clipping or overlap. Repository harness validation passed; frontend
  `/` and backend `/test` returned HTTP 200. MySQL and Redis remained healthy
  and `db-migrate` remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-sudoku-item-empty/sudoku-item-desktop.png`
- `artifacts/runs/20260714-legacy-sudoku-item-empty/sudoku-item-mobile.png`

## Risks And Follow-Ups

- The seeded Item panel currently has six SimpleData fields; the zero-field
  branch is covered by the source contract and the shared row-padding logic.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
