# Sudoku Chart Presentation Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Compare old `linechart.jade` and `.sw-partialchart` sizing against the
  deployed Vue Sudoku chart.
- Add a compact mode to the shared chart instead of changing normal
  `viewWithChart` rendering.
- Keep the compact SVG full width and prevent mobile axis-label overlap.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/SudokuPanels.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T12-04-00Z-sudoku-chart-parity.md`

## Legacy Evidence

- `views/includes/linechart.jade` renders the ECharts target through
  `.sw-partialchart` inside the Sudoku flow-control shell.
- `public/stylesheets/soway.css` fixes `.sw-partialchart` at 200px high.
- The shared Vue chart previously inherited a 280px pane minimum and a 260px
  SVG minimum, producing a 637px-tall deployed Sudoku panel at desktop width.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:dd7edf239ab1284d6bb0c63c214cb21ee89a5133ddb00a95cfc2fc5a796635ad`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view103`: the compact pane measured 1198x200px, the
  SVG measured 1172x147px with a 1275x160 viewBox, and the legend remained
  visible.
- At 390x844, the compact pane measured 328x200px, the page had no horizontal
  overflow, and only the first/last axis labels rendered; their bounding boxes
  did not overlap.
- Authenticated `/view100`: no compact pane rendered and the shared normal
  chart retained its 720x300 viewBox and non-compact height.
- Browser logs were empty.

## Runtime Evidence

- `artifacts/runs/20260713-sudoku-chart-parity/desktop.jpg`
- `artifacts/runs/20260713-sudoku-chart-parity/mobile.jpg`

## Risks And Follow-Ups

- Old `Sudoku.js` still applies one maximum height to every flow-control shell;
  restore that separately now that map and chart intrinsic heights match their
  fixed 200px legacy partials.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
