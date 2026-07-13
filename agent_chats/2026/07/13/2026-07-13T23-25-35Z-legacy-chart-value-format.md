# Legacy Chart Value Format

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Resolve chart label, axis, and tooltip text from the old `swchartLine.js`
  configuration plus ECharts 3.1.7 source.
- Preserve View `FmtValue` text beside numeric chart geometry without binding
  rendering to a business DTO.
- Share one minimal formatting implementation across top-level and compact
  chart entrypoints.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/legacyChartFormat.ts`
- `frontend/src/legacyChartFormat.test.ts`
- `frontend/src/LegacyChartPanel.vue`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T23-25-35Z-legacy-chart-value-format.md`

## Legacy Evidence

- Both branches in
  `../FoolFrame/src/Web/public/javascripts/app/swchartLine.js` push each chart
  item's View `FmtValue` directly into the ECharts series data array.
- ECharts 3.1.7 `BarView` falls back from an absent label formatter to
  `seriesModel.getRawValue`, preserving the option string including trailing
  decimal zeroes.
- ECharts `Symbol` falls back to the numeric data dimension for scatter label
  text, so scatter labels do not retain the raw string's zeroes or commas.
- `Series.formatTooltip` uses the raw series value and `format.addCommas`.
  That helper inserts commas into the integer part but preserves the complete
  decimal substring.
- The deployed Vue baseline rendered the seeded nonzero bar labels as `1.5`
  and `0.25`; its category-1002 tooltip showed
  `Amount : 1.5 / Price : 3,450`.

## Implementation

- Added optional `formattedValues` to the shared chart series and populated it
  from View `FmtValue` while retaining the existing numeric `values` for scale,
  stacking, paths, and hit testing.
- Carried the same sequence through the existing 100-point realtime append
  helper, including its initial zero window.
- Added the 19-line `legacyChartFormat.ts` helper. It reproduces ECharts 3
  comma insertion, uses View text for bar labels, numeric values for scatter
  labels, and View text plus commas for tooltips.
- Removed the locale-dependent, two-decimal `Intl.NumberFormat` from the shared
  renderer. No component, request, DTO branch, chart path, or dependency was
  duplicated.

## Validation

- Focused chart/view tests: 3 files and 56 tests passed.
- Full `npm test`: 14 files and 174 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `git diff --check` passed before documentation updates.
- Frontend Docker build and no-dependency forced recreation passed. Deployed
  image id:
  `sha256:04d79ff9c2314825ae442104c770e8b0d4e32a708ee3f8beb0c8bdf93b6f6f7d`.
- Backend `/test` passed; MySQL/Redis were healthy and `db-migrate` remained
  `Exited (0)`.
- Authenticated desktop `/view100` rendered bar labels `1.5000000000` and
  `0.2500000000`, retained `100,000`-style value-axis ticks, and showed the
  full category-1002 tooltip inside the 1280x720 viewport.
- At 390x844, the same full-precision tooltip remained contained. After pointer
  repositioning settled, the chart pane width and scroll width were both 330px,
  and no value label escaped the pane.
- Mobile `/view103` retained 100 bar labels and a contained 328x200 chart. Its
  last label was `0.2500000000`; the category-1001 tooltip showed
  `Amount : 0.2500000000 / Price : 62,500.0000000000` and stayed inside the
  compact pane.
- A temporary View metadata probe changed Price from `EditType=12` to `14`.
  Its eight scatter labels were numeric (`3450`, `62500`) while the tooltip
  retained `3,450.0000000000`.
- Both `fool_sys_view_item.id=1007` and `SW_SYS_VIEW_ITEM.SysId=1007` were
  restored to `12`. After backend restart, `/view100` returned to eight bars,
  one line, zero scatter marks, and full View-formatted bar labels.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-value-format/order-chart-value-format-desktop.png`
- `artifacts/runs/20260714-legacy-chart-value-format/order-chart-value-format-mobile.png`
- `artifacts/runs/20260714-legacy-chart-value-format/sudoku-value-format-mobile.png`

## Skipped Checks And Risks

- The old FoolFrame app was not separately booted; text semantics were resolved
  from its exact `FmtValue` series population and pinned ECharts source chain.
- Browser console logs were not separately collected. DOM text, geometry,
  containment, screenshots, backend health, tests, and build were checked.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.
