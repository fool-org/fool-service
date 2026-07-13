# Legacy Chart Scatter Symbol

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Trace old scatter defaults through `swchartLine.js` and ECharts 3.1.7.
- Restore the shared scatter marker's rendered size, opacity, and emphasis
  target without changing View projection, chart geometry, or dependencies.
- Verify the same marker contract at desktop and mobile sizes, then restore the
  seeded runtime metadata used for the probe.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T22-39-01Z-legacy-chart-scatter-symbol.md`

## Legacy Evidence

- Both old `swchartLine.js` chart branches map `EditType=14` to scatter and set
  `symbol: 'circle'` without `symbolSize` or `itemStyle` overrides.
- ECharts 3.1.7 `ScatterSeries.js` defaults `symbolSize` to 10 and normal
  opacity to 0.8.
- ECharts 3.1.7 `Symbol.js` creates a unit symbol scaled to `symbolSize` and
  expands emphasis to `max(size * 1.1, size + 3)`, making 10px become 13px.

## Implementation

- Added one computed radius that reuses the shared chart's existing
  viewBox-to-rendered-width ratio, so a nominal 5px radius stays 5 actual
  rendered pixels at every responsive width.
- Set scatter opacity to 0.8 and changed the existing emphasis scale from 1.25
  to 1.3, producing the old 10-to-13px transition.
- Added no component, helper file, dependency, business DTO branch, API request,
  or duplicate renderer. `LegacyChartPanel.vue` remains 320 lines.

## Validation

- Focused `npm test -- style.test.ts`: 4 tests passed.
- Full `npm test`: 13 files and 168 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:e228dc2c3a7a554adf03c693ff4b4040b9752be582d6497154ec90be3e1b3629`.
- Backend `/test` and Compose checks passed; MySQL/Redis were healthy and
  `db-migrate` remained `Exited (0)`.
- Temporarily changed seeded Price metadata from `EditType=12` to `14` in both
  metadata tables and restarted the backend. Authenticated `/view100` rendered
  eight scatter marks with exact 10px actual diameter and opacity 0.8 at
  1280x720 and 390x844. Desktop/mobile chart heights remained 414px/470px,
  mobile retained two category labels, and neither viewport overflowed.
- Price legend click hid and restored the scatter series. Browser warnings and
  errors were empty.
- Restored both Price metadata rows to `EditType=12`, restarted the backend,
  and confirmed the normal page again had one Price line and zero scatter marks.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-scatter-symbol/order-chart-scatter-desktop.png`
- `artifacts/runs/20260714-legacy-chart-scatter-symbol/order-chart-scatter-mobile.png`

## Skipped Checks And Risks

- The browser surface still cannot establish CSS hover state; the 1.3 emphasis
  scale is therefore covered by the focused production-source contract rather
  than a direct pointer screenshot. Base marker geometry, opacity, responsive
  size, legend selection, and restored default runtime state were observed.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
