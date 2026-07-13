# Legacy Chart Item Hover

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Trace old direct item hover behavior for bar, scatter, and line graphics.
- Restore bar/scatter pointer targets without regressing the axis tooltip,
  legend selection, responsive chart geometry, or shared View projection.
- Verify desktop/mobile hit targets with real View metadata and restore the
  temporary scatter probe before delivery.

## Changed Files

- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T22-51-41Z-legacy-chart-item-hover.md`

## Legacy Evidence

- ECharts 3.1.7 `BarView.js` calls `graphic.setHoverStyle` for every bar.
- ECharts 3.1.7 `Symbol.js` binds mouseover/mouseout color emphasis and the
  400ms symbol-size animation to every scatter symbol.
- ECharts 3.1.7 `LineView.js` creates its polyline and area polygon with
  `silent: true`, so this direct item slice applies only to bars and scatter.
- Before the change, the seeded nonzero Amount bar's center resolved through
  `document.elementFromPoint` to the later-painted `.chart-axis-hit` rectangle.

## Implementation

- Moved the existing axis-tooltip mousemove/mouseleave listeners to the shared
  SVG root so events from either the background or a series item use the same
  tooltip path.
- Painted the existing transparent hit rectangle before the series groups,
  making bars and scatter symbols the actual pointer targets.
- Reused the existing 1.1 brightness lift and 1.3 scatter scale for direct item
  CSS hover. No new state, helper, component, dependency, DTO branch, request,
  or renderer was added. `LegacyChartPanel.vue` remains 327 lines.

## Validation

- Focused `npm test -- style.test.ts payload.test.ts`: 87 tests passed.
- Full `npm test`: 13 files and 169 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `python scripts/check_repo_harness.py` and `git diff --check` passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:8956225ca2639b9a95f788a17df75c7653b6ac7b646d6b8cdda01b1002c68d58`.
- Backend `/test` and Compose checks passed; MySQL/Redis were healthy and
  `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view100` changed the same nonzero Amount bar center
  from `.chart-axis-hit` to `.chart-bar`. Pointer movement retained the axis
  pointer and `1002 / Amount: 1.5` tooltip while Price was legend-hidden.
- A temporary, restored Price `EditType=14` probe rendered eight scatter marks;
  all eight centers resolved to `.chart-scatter`. Pointer movement showed the
  `1002 / Amount: 1.5 / Price: 3,450` tooltip at desktop and 390x844.
- Mobile retained a 470px chart, two category labels, no document overflow,
  and empty warning/error logs. Both metadata rows were restored to
  `EditType=12`; the normal page again had eight bars, one line, zero scatter.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-item-hover/order-chart-item-hover-desktop.png`
- `artifacts/runs/20260714-legacy-chart-item-hover/order-chart-item-hover-mobile.png`

## Skipped Checks And Risks

- The browser control surface moved a real pointer and fired the SVG mousemove
  path, but still did not expose CSS `:hover` state. Direct brightness/scale
  rendering therefore remains covered by focused production-source contracts;
  actual bar/scatter hit targeting and tooltip behavior were observed directly.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.
