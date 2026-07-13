# Legacy Chart Tooltip Bounds

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Compare the old ECharts axis-tooltip hit boundary with the shared Vue chart.
- Stop top-level and compact charts from retaining tooltips outside the plot.
- Keep the fix inside the existing renderer without adding a component,
  helper, DTO branch, request, or dependency.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T23-40-29Z-legacy-chart-tooltip-bounds.md`

## Legacy Evidence

- ECharts 3.1.7 `TooltipView._showAxisTooltip` builds the pointer point from
  both event offsets, calls `coordSys.containPoint(point)`, and invokes `_hide`
  when no coordinate system contains it.
- An exact 720x300 old-option probe measured the legacy grid as
  `x=0, y=60, width=576, height=215`. Moving inside showed the axis tooltip;
  moving to the top whitespace hid it.
- The deployed Vue baseline showed `9851342 / Amount : 0E-10 / Price : 0E-10`
  inside the plot and incorrectly retained the same tooltip at the same x
  coordinate in the SVG's top whitespace.

## Implementation

- Converted `clientY` into the existing chart viewBox alongside `clientX`.
- Cleared the active tooltip when either coordinate falls outside the shared
  `plot` bounds before calculating the nearest category.
- Added source-contract coverage to the existing chart-style test. The shared
  renderer grew by five lines; no rendering path or boundary calculation was
  duplicated.

## Validation

- Focused `npm test -- --run src/style.test.ts`: 8 tests passed.
- Full `npm test`: 14 files and 174 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `git diff --check` passed before documentation updates.
- `docker compose up -d --build` passed; the frontend was then explicitly
  recreated because the first Compose run left the previous container attached
  to its old image. Deployed image id:
  `sha256:546232e72b4aeb567e0372e36b21d91ea5b87b061826b6216eaae34c665481b6`.
- Backend `/test` passed; MySQL/Redis were healthy and `db-migrate` was
  `Exited (0)`.
- Authenticated desktop `/view100` showed one full axis tooltip inside and zero
  tooltips in both top and bottom out-of-plot regions.
- At 390x844, `/view100` passed the same inside/top/bottom checks and retained
  its readable responsive chart, legend, category labels, and pagination.
- Mobile `/view103` showed `Amount : 0 / Price : 0` inside its 328x200 compact
  plot and zero tooltips above and below it.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-tooltip-bounds/legacy-echarts-tooltip-inside.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-bounds/legacy-echarts-tooltip-outside.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-bounds/order-tooltip-inside-desktop.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-bounds/order-tooltip-top-outside-desktop.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-bounds/order-tooltip-inside-mobile.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-bounds/order-tooltip-outside-mobile.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-bounds/sudoku-tooltip-inside-mobile.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-bounds/sudoku-tooltip-outside-mobile.png`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; its pinned ECharts source
  and an exact old-option ECharts 3.1.7 browser probe supplied legacy behavior.
- Browser console logs were not separately collected. DOM tooltip counts,
  visible text, screenshots, Docker state, backend health, tests, and build
  were checked.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.
