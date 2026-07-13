# Legacy Chart Legend Glyphs

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Compare the shared Vue legend with ECharts 3.1.7's exact legend model and
  symbol rendering defaults used by old `swchartLine.js`.
- Restore item dimensions, typography, disabled color, and type-specific bar,
  line, and scatter glyphs in both chart entrypoints.
- Preserve the old 20% right grid while preventing wider View-derived legend
  names from entering the plot on narrow screens.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T22-15-16Z-legacy-chart-legend-glyphs.md`

## Legacy Evidence

- `../FoolFrame/src/Web/package.json` declares ECharts `^3.1.7`, and both
  `swchartLine.js` paths leave legend item/text styling at library defaults.
- ECharts 3.1.7 `LegendModel.js` defines 25x14 items, 10px item gaps, `#333`
  normal text, 5px padding, and selected mode enabled.
- `LegendView.js` and `util/symbol.js` render bar as a 3.5px-radius roundRect,
  line as a 25px line plus centered 11.2px circle when `symbol: 'none'`, and
  scatter as a centered 14px circle. Unselected glyph and text color is
  `#ccc`.

## Implementation

- Replaced the one generic 10px color block and bold label with one shared
  typed symbol span and normal label. CSS `currentColor` reuses the existing
  selected/hidden state without a new helper, dependency, or child component.
- Restored the old dimensions, gaps, right inset, text metrics, and exact
  bar/line/scatter shapes; narrow layouts keep only the existing vertical
  responsive orientation.
- Reused the chart's existing ResizeObserver for legend width. Plot right
  remains 20% unless the rendered legend plus its 5px inset is wider, avoiding
  hard-coded View names and preserving View-first data ownership.
- Moved the responsive plot-width assertion into focused `style.test.ts` and
  removed the obsolete fixed-20% duplicate from the oversized payload test.

## Validation

- Full frontend suite: 166 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:6fd009157b55be27902f83c8d1281b6ea6b8ec959666ada2b71b16c04eb7cdcd`.
- Backend `/test` and Compose checks passed; MySQL/Redis were healthy and
  `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view100` rendered a 25x14 Amount bar glyph and a
  25x14 Price line glyph with its 11.2px circle. Both labels computed as
  `12px/400/#333`, icon/text gap was 5px, right inset was 5px, and the legend
  retained 92.28px clearance from the plot in the 414px chart.
- Clicking Amount changed `aria-pressed` to false and retained 25x14 geometry
  while both glyph and text became `rgb(204, 204, 204)`.
- At 390x844, `/view100` retained its 470px chart and two category labels; its
  73.98px legend met the plot at x=281.02 without overlap or document overflow.
- Authenticated `/view103` retained its 328x200 compact pane, two labels, and
  the same zero-overlap legend boundary. Browser warnings/errors were empty.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-legend-glyphs/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-legend-glyphs/order-chart-mobile.png`

## Risks And Follow-Ups

- This slice restores legend presentation and selection-state visuals. The old
  ECharts legend hover-highlight interaction remains a separate parity item.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
