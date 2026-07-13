# Legacy Chart Legend Hover

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Trace old legend mouseover/mouseout through ECharts 3.1.7 highlight and
  downplay behavior.
- Restore the shared series-name hover link without changing click selection,
  tooltip state, View data projection, or chart geometry.
- Preserve top-level and compact chart behavior at desktop and mobile sizes.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T22-25-32Z-legacy-chart-legend-hover.md`

## Legacy Evidence

- ECharts 3.1.7 `LegendView.js` binds mouseover to a series-name `highlight`
  action and mouseout to `downplay` when `legendHoverLink` is enabled.
- `graphic.js` uses zrender `color.lift(color, -0.1)` for default emphasis;
  zrender 3.0.8 multiplies each RGB channel by 1.1 for that level.
- Bar rectangles receive that default lifted fill. Scatter symbols also run
  the 400ms hover animation to `max(size * 1.1, size + 3)`.
- The old line options set `symbol: 'none'`; whole-series highlight has no line
  point symbol to emphasize through the generic ChartView data-element path.

## Implementation

- Added one nullable `hoveredSeriesName` ref. Legend enter/leave assigns or
  clears the existing metadata-derived series name, and each existing series
  group compares against that name for its emphasis class.
- Shared CSS applies `brightness(1.1)` to emphasized bar/scatter graphics and
  expands the current 12px scatter to 15px over 400ms. The legend button uses
  the same lift on native CSS hover.
- No new component, helper, dependency, business DTO, API request, duplicated
  renderer, or geometry branch was added.

## Validation

- Full frontend suite: 167 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:0c086ff6224d9ffe1862e794448dbdbf003126e4f20db5f2a773906b48fc4341`.
- Backend `/test` and Compose checks passed; MySQL/Redis were healthy and
  `db-migrate` remained `Exited (0)`.
- Authenticated `/view100` retained working Amount click hide/show state.
  Desktop stayed at 414px; at 390x844 it stayed 470px with two labels, both
  legends selected, no document overflow, and the legend inside the viewport.
- Authenticated `/view103` retained its 328x200 compact pane, two labels, and
  contained legend at 390x844. All checked browser consoles were empty.
- Contract tests verify the enter/leave bindings, metadata-name class link,
  bar/scatter 1.1 lift, scatter 1.25 transform, and legend native hover lift.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-legend-hover/order-chart-desktop.png`
- `artifacts/runs/20260714-legacy-chart-legend-hover/order-chart-mobile.png`

## Skipped Checks And Risks

- The browser control surface successfully clicked exact pointer coordinates
  but did not establish CSS `:hover` or fire DOM `mouseenter`; direct pointer
  observation of the highlight/downplay transition was therefore unavailable.
  The screenshots prove surrounding layout only, not the hover visual itself.
- A real manual pointer pass remains the residual check for this interaction.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
