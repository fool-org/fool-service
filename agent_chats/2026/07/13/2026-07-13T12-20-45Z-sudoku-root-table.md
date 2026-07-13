# Sudoku Root Table Boundary

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Compare old `Sudoku.jade` root rendering against the deployed Vue page.
- Remove the shared root list table from Sudoku without changing child panels.
- Prove normal list and chart templates still render their root tables.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T12-20-45Z-sudoku-root-table.md`

## Legacy Evidence

- `views/Sudoku.jade` iterates root `Items` and includes only the matching
  List, Group, Map, Item, or linechart partial in each column.
- It has no root data-table or root pagination block after those child panels.
- The Vue table's `v-show` condition treated the shared `activePane=table`
  state as sufficient even for Sudoku, producing one visible empty table with
  the five panel names as headers.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:0883633fedb567172ae4ca946203fa2250a76d9e9372faaa7a4ccb4b27d888bb`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view103`: five Sudoku panels remained visible and
  equal at 458.148px, while direct root-table and root-pagination counts were
  both zero.
- Authenticated `/view100` and `/view101`: chart and normal list pages each
  retained one visible root table.
- At 390x844, `/view103` retained five panels, no root table, no horizontal
  overflow, and empty browser logs.

## Runtime Evidence

- `artifacts/runs/20260713-sudoku-root-table/desktop.jpg`
- `artifacts/runs/20260713-sudoku-root-table/mobile.jpg`

## Risks And Follow-Ups

- The root pagination was already absent at runtime because Sudoku skips the
  root data query; this slice changes only the confirmed visible root-table
  leak and does not broaden pagination behavior.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
