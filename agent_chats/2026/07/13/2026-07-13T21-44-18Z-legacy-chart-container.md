# Legacy Chart Container

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Compare the shared Vue chart container with both old chart templates and
  their stylesheet contracts.
- Remove only the confirmed Vue-only frame, leaving data, drawing geometry,
  top-level height, and compact fixed height unchanged.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T21-44-18Z-legacy-chart-container.md`

## Legacy Evidence

- `viewWithChart.jade` uses the chart tab pane itself as the ECharts mount and
  applies no chart-specific frame class.
- `includes/linechart.jade` mounts ECharts in `.sw-partialchart`; old
  `soway.css` gives that class only `width: 100%; height: 200px`.
- ECharts initializes directly against those elements. No old template,
  controller, or chart stylesheet adds a border, radius, or 12px inset.

## Implementation

- Removed the shared pane's 1px slate border, 6px radius, and 12px padding.
- Added a focused raw-style contract around the existing pane block. No new
  class, component, dependency, renderer, or View/business DTO path was added.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:1e025328032cf84e84b229990d424784304ea300db07fc69543cb38bbd381cbe`.
- Backend `/test` and Compose service checks passed; MySQL/Redis were healthy
  and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` computed zero border/radius/padding. At 1440x1000,
  pane and SVG shared origin `40,235` and width `1360`; at 390x844 they shared
  origin `30,403.84` and width `330`.
- Authenticated `/view103` retained a 200px compact pane/SVG at desktop and
  mobile, with zero horizontal inset. Browser warnings/errors were empty and
  both pages retained document-width equality with the viewport.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-container/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-container/order-chart-mobile.png`
- `artifacts/runs/20260714-legacy-chart-container/sudoku-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-container/sudoku-chart-mobile.png`

## Risks And Follow-Ups

- Old `viewWithChart.js` also sets the top-level chart tab's height from the
  data tab on first load. That separate layout behavior was not mixed into this
  frame-only change and remains the next comparison candidate.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
