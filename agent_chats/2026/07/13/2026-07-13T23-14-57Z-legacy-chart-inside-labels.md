# Legacy Chart Inside Labels

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Resolve the old bar/scatter value-label defaults from `swchartLine.js` and
  ECharts 3.1.7 source.
- Correct only label geometry, typography, fill, and scatter opacity in the
  existing shared chart renderer.
- Exercise top-level bar/scatter and compact chart paths without adding a
  component, dependency, DTO branch, request, or duplicate renderer.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T23-14-57Z-legacy-chart-inside-labels.md`

## Legacy Evidence

- Both branches in
  `../FoolFrame/src/Web/public/javascripts/app/swchartLine.js` set
  `label.normal.show: true` for bar/scatter series without a position, color,
  font-size, or opacity override.
- ECharts 3.1.7 `graphic.setText` defaults an unspecified label position to
  `inside` and selects white for an inside label.
- Its text-style default resolves to normal 12px sans-serif. ZRender's
  `adjustTextPositionOnRect` centers `inside` text within the owning rectangle.
- `BarView` attaches that text to each bar rectangle. `Symbol` attaches it to
  each scatter symbol, whose seeded old default is opacity 0.8.
- The deployed Vue baseline used 10px `#333` labels positioned seven SVG units
  above values. The first nonzero `/view100` bar had label y `246.99646` and
  bar y `253.99646` instead of an inside position.

## Implementation

- Reused existing bar and scale helpers to return each bar rectangle center or
  scatter point center from `valueLabelY`.
- Added SVG central-baseline alignment, changed shared label style to 12px
  white, and applied 0.8 opacity only to scatter labels.
- Kept the existing line-label guard, raw value formatting, tooltip behavior,
  legend selection, stacked geometry, and both chart entrypoints unchanged.
- The shared component remains 336 lines; no abstraction or dependency was
  introduced for the three-line geometry contract.

## Validation

- Focused `npm test -- --run src/style.test.ts`: 8 tests passed.
- Full `npm test`: 13 files and 172 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `git diff --check` passed before documentation updates.
- `docker compose up -d --build` and forced frontend recreation passed.
  Deployed frontend image id:
  `sha256:a26649e8d3a10be69bd2478d73276288abba43e5d1a7f8f19c5baa86fd908d72`.
- Backend `/test` passed; MySQL/Redis were healthy and `db-migrate` was
  `Exited (0)`.
- Authenticated desktop `/view100` rendered eight bar labels with computed
  12px white style and central baseline. After hiding Price, the first nonzero
  label and bar-center y coordinates were both `253.99823`.
- At 390x844, `/view100` retained eight labels in a 330px-wide pane with
  `scrollWidth == clientWidth` and zero labels outside the viewport.
- Mobile `/view103` retained 100 bar labels, zero label viewport escapes, and
  a contained 328x200 compact chart.
- A temporary View metadata probe changed Price from `EditType=12` to `14`.
  `/view100` rendered eight scatter marks and eight scatter labels; the first
  label had zero x/y center delta, 12px white style, and opacity 0.8.
- Both `fool_sys_view_item.id=1007` and `SW_SYS_VIEW_ITEM.SysId=1007` were
  restored to `12`. After backend restart, `/view100` returned to eight bars,
  one line, zero scatter marks, and eight labels.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-inside-labels/order-chart-inside-labels-desktop.png`
- `artifacts/runs/20260714-legacy-chart-inside-labels/order-chart-inside-labels-mobile.png`
- `artifacts/runs/20260714-legacy-chart-inside-labels/order-chart-scatter-labels-mobile.png`
- `artifacts/runs/20260714-legacy-chart-inside-labels/sudoku-inside-labels-mobile-viewport.png`

## Skipped Checks And Risks

- The old FoolFrame app was not separately booted; behavior was resolved from
  its exact chart configuration and the pinned ECharts/ZRender source chain.
- Browser console logs were not separately collected in this slice. DOM
  geometry, computed style, responsive containment, screenshots, backend
  health, and frontend tests/build were checked directly.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.
