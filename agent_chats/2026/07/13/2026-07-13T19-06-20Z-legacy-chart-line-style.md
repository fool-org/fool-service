# Legacy Chart Line Style

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and keeping file size and reuse under control.

## Scope

- Compare the deployed shared SVG chart with old `swchartLine.js`.
- Restore the configured line-series shape, fill, symbol, label, and point
  tooltip behavior without changing View-first chart data.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T19-06-20Z-legacy-chart-line-style.md`

## Legacy Evidence

- `swchartLine.js getchartoption` configures line series with `smooth: true`,
  `symbol: 'none'`, `areaStyle`, and visible labels.
- The same script configures visible labels for bar and scatter series and uses
  ECharts tooltips for chart values.
- Deployed `/view100` previously rendered the Price series as straight SVG
  segments with visible point circles and no value labels or area fill.

## Implementation

- Replaced the shared line polyline with one smooth cubic path plus a baseline
  area path.
- Removed visible line point symbols while retaining transparent hit targets
  with metadata-derived native tooltips.
- Added value labels for line, bar, and scatter points through the existing
  shared renderer used by top-level and Sudoku charts.
- Added no dependency, chart-specific DTO, second renderer, or data request.

## Validation

- Focused payload contract: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:17a11ea0fec5beb90ede9e682d0e22198ffe8369fc310a2792ddf3bb8ca707c4`.
- Backend `/test`, frontend `/`, and Compose service-state checks passed;
  MySQL/Redis were healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` rendered one line area/path, eight transparent line
  hit targets, sixteen value labels, eight bars, and zero visible line circles.
- The final Price point exposed `Price · 1001: 62,500`; desktop 1440x1000 and
  mobile 390x844 screenshots showed the curve, labels, legend, and controls
  without overlap or horizontal clipping.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-series/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-series/order-chart-mobile.png`

## Risks And Follow-Ups

- Visible value labels intentionally inherit old ECharts behavior and may be
  dense when a View returns many series or closely spaced values.
- Native SVG point titles cover the migrated per-value tooltip need but do not
  reproduce ECharts' full cross-axis tooltip overlay.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
