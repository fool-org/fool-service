# Sudoku Root Heading Boundary

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Compare old `Sudoku.jade` root content against the deployed Vue heading.
- Hide the shared root View title only for Sudoku templates.
- Prove normal list View titles and all Sudoku child titles remain visible.

## Changed Files

- `frontend/src/ViewListPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T12-35-06Z-sudoku-root-heading.md`

## Legacy Evidence

- `views/Sudoku.jade` starts its content block with `.row` and iterates child
  View items; it never renders `view.data.Name` as a page heading.
- The shared Vue panel previously rendered `Order Sudoku` above the five child
  panels even after its root toolbar and root table had been removed.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:05d90d992128141a3d3ed1b5e0e21fb928765f01dbb0cdcff1f7df179c28c8b8`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view103`: no shared root heading or root table
  remained, while Orders List, Price Chart, Customer Map, Order Item, and
  Order Group titles all remained visible and rows stayed locked at 458.148px.
- Authenticated `/view101`: `Order Item List` and its root table remained
  visible.
- At 390x844, root-heading count was zero, child-heading count was five,
  `scrollWidth` equaled 390, and browser logs were empty.

## Runtime Evidence

- `artifacts/runs/20260713-sudoku-root-heading/desktop.jpg`
- `artifacts/runs/20260713-sudoku-root-heading/mobile.jpg`

## Risks And Follow-Ups

- The shared article frame remains as a deliberate Vue presentation layer;
  this slice removes only the confirmed title content absent from the old
  template.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
