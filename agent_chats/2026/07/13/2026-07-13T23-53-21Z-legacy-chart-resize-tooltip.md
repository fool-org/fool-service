# Legacy Chart Resize Tooltip

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Compare the old ECharts tooltip state transition during resize with Vue.
- Refresh or clear the shared top-level and compact tooltip after responsive
  chart or legend geometry changes.
- Keep the fix inside the existing renderer without adding a component,
  request, DTO branch, rendering path, or dependency.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T23-53-21Z-legacy-chart-resize-tooltip.md`

## Legacy Evidence

- ECharts 3.1.7 `ECharts.resize` resizes zrender and updates the chart.
- `TooltipView.render` schedules `_manuallyShowTip` with `_lastX` and `_lastY`
  after the update; `_tryShow` stores those canvas-local event offsets.
- Replaying those coordinates through `_showAxisTooltip` makes the resized
  coordinate system either choose its new category or hide outside the grid.
- In the deployed Vue baseline, desktop `/view100` showed the category-1002
  tooltip, then shrinking directly to 390x844 left it at the old desktop
  position. The 330px pane had 852px scroll width and its full-page screenshot
  expanded to 882px.

## Implementation

- Retained the last valid pointer position relative to the rendered SVG.
- Reused `showAxisTooltip` after chart and legend ResizeObserver updates so the
  existing plot bounds, nearest-category, alignment, and positioning logic stay
  authoritative.
- Cleared the retained point with every existing tooltip hide path, including
  legend selection. The shared component grew from 341 to 354 lines.

## Validation

- Focused `npm test -- src/style.test.ts`: 9 tests passed.
- Full `npm test`: 14 files and 175 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose up -d --build` passed. Compose again retained the previous
  frontend container, so `docker compose up -d --force-recreate frontend`
  attached the rebuilt image:
  `sha256:58ce23aa75747250dd7a998f513f2f1696b408a7658728ef8411ff552f9520b9`.
- Backend `/test` passed; MySQL/Redis were healthy and `db-migrate` was
  `Exited (0)`.
- Authenticated desktop `/view100` showed the category-1002 tooltip at the
  prior failing point. After direct resize to 390x844, the point was outside
  the new plot, tooltip count became zero, and full-page width was 390px rather
  than the 882px baseline.
- A second `/view100` point that remained in the resized plot kept one tooltip
  and recalculated its nearest category, confirming the replay path rather than
  unconditional hiding.
- Desktop `/view103` showed one compact tooltip; the same direct resize moved
  that saved point outside the 328x200 plot and reduced tooltip count to zero.
- Browser logs were empty.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-resize-tooltip/baseline-mobile-after-resize.png`
- `artifacts/runs/20260714-legacy-chart-resize-tooltip/fixed-tooltip-desktop.png`
- `artifacts/runs/20260714-legacy-chart-resize-tooltip/fixed-after-mobile-resize.png`
- `artifacts/runs/20260714-legacy-chart-resize-tooltip/fixed-compact-after-mobile-resize.png`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; its pinned ECharts 3.1.7
  source supplied the legacy resize behavior.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.
