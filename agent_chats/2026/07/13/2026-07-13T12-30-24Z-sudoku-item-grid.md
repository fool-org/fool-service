# Sudoku Item Field Grid

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse components.

## Scope

- Compare old `subitem.js` SimpleData table construction against the deployed
  Vue Sudoku Item panel.
- Render two metadata-derived fields per table row and pad to six rows.
- Extract the table into a small component instead of growing the Sudoku panel
  dispatcher.

## Changed Files

- `frontend/src/LegacyItemPanel.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T12-30-24Z-sudoku-item-grid.md`

## Legacy Evidence

- `public/javascripts/app/subitem.js` iterates `Data.SimpleData`, writes one
  label/value pair at a time, opens a row on even indexes, and closes it after
  the second field.
- It pads the generated table with four-cell blank rows until at least six
  rows exist.
- The Vue panel previously rendered all six fields as six separate two-cell
  rows and had no legacy blank-row padding.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:35bc8c5af5c5c433a4e127a5698d9d9ffb4c0370eba573e58d11cad571f1e078`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view103`: Item retained six rows, each with an
  effective four columns; the six fields occupied the first three rows and
  the last three were blank. The global 458.148px panel lock remained intact.
- At 390x844, all six rows retained four effective columns, the 326px table
  stayed inside its 330px panel, page `scrollWidth` equaled 390, and browser
  logs were empty.
- `SudokuPanels.vue` is 173 lines and the extracted `LegacyItemPanel.vue` is
  29 lines.

## Runtime Evidence

- `artifacts/runs/20260713-sudoku-item-grid/desktop.jpg`
- `artifacts/runs/20260713-sudoku-item-grid/mobile.jpg`

## Risks And Follow-Ups

- Old Item heading/footer text (`详细`, update time, and the no-op refresh link)
  remains a separate presentation slice; this change covers only the confirmed
  field-table structure.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
