# Legacy Chart Category Boundary

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Compare old `swchartLine.js` category-axis placement with the deployed shared
  SVG chart.
- Restore the old no-gap category boundary without duplicating series geometry.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T19-19-39Z-legacy-chart-category-boundary.md`

## Legacy Evidence

- `swchartLine.js getchartoption` configures the shared category x-axis with
  `boundaryGap: false` before adding line, bar, and scatter series.
- The deployed renderer instead used `(index + 0.5) / labelCount`, leaving a
  half-category gap before the first and after the last data point.
- On the eight-category `/view100` chart, that placed centers at `92.625` and
  `661.375` while the horizontal axis spanned `52` through `702`.

## Implementation

- Changed the existing shared `x()` function to interpolate multi-category
  centers from the first axis endpoint to the last axis endpoint.
- Kept a one-category midpoint fallback, avoiding a special series branch and
  division by zero.
- Reused that one coordinate for line, bar, scatter, value labels, and category
  labels. Moved the axis name to its own final line after browser measurement
  found that the newly aligned last category otherwise overlapped it.
- Added no dependency, business DTO path, component, or duplicated renderer.

## Validation

- Focused chart/source contracts: 82 tests passed.
- Full frontend suite: 156 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:2b282aea37d888e6c43d4e9ecbfec3d428e10f565f05943c5ff3f24d6f2bde01`.
- Backend `/test`, frontend `/view100`, and Compose service checks passed;
  MySQL/Redis were healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` at `1440x1000` and `390x844` rendered eight line
  hit targets with first/last x coordinates `52/702`, exactly matching the
  horizontal-axis endpoints. The axis name did not overlap the final category;
  mobile checks also kept the first label, last label, and axis name inside the
  SVG bounds.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-boundary-gap/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-boundary-gap/order-chart-mobile.png`

## Risks And Follow-Ups

- Bar widths still use the shared renderer's existing cap; legacy
  `barMaxWidth: 15` remains a separate visible parity item.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
