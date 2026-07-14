# Legacy Chart Tooltip Style

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Compare the old ECharts tooltip box and movement defaults with Vue.
- Restore the shared top-level/compact tooltip metrics and pointer movement.
- Keep the change CSS-only apart from its existing source-contract test.

## Changed Files

- `frontend/src/style.css`
- `frontend/src/style.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T00-10-42Z-legacy-chart-tooltip-style.md`

## Legacy Evidence

- ECharts 3.1.7 `TooltipContent` renders an absolute block with nowrap text,
  `z-index:9999999`, and 21px line height for its default 14px font.
- Its `refixTooltipPosition` uses a 20px pointer gap.
- Its default `transitionDuration: 0.4` assembles `left` and `top` transitions
  with `cubic-bezier(0.23, 1, 0.32, 1)`.
- An exact 720x300 browser probe measured a three-row tooltip at 73px high with
  `display:block`, `line-height:21px`, dual 0.4s transitions, and nowrap text.
- The deployed Vue baseline measured `display:grid`, a 2px gap, inherited
  `line-height:20.3px`, 74.89px height, an 8px transform, `z-index:2`, normal
  container whitespace, and zero transition duration.

## Implementation

- Matched the old block, line-height, nowrap, z-index, 20px directional gap,
  and exact `left/top` transition in the existing shared tooltip selector.
- Removed the now-redundant child nowrap selector and the Vue-only grid gap.
- `style.css` shrank by three lines; no component, helper, request, DTO branch,
  rendering path, dependency, or duplicate chart style was added.

## Validation

- Focused `npm test -- src/style.test.ts`: 11 tests passed.
- Full `npm test`: 14 files and 177 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose build frontend` passed; the service was attached with
  `docker compose up -d --no-deps --force-recreate frontend` to image
  `sha256:d0773decc43337eb65fcd489fa936c0de787880fc88933b3dfec5bae7cb33d4d`.
- Backend `/test` passed; MySQL/Redis were healthy and `db-migrate` remained
  `Exited (0)`.
- Authenticated desktop `/view100` measured `display:block`, 73px height, 21px
  line height, 20px transform, nowrap text, `z-index:9999999`, and the exact
  dual 0.4s transition curve.
- Moving the tooltip from left 260px to 460px measured 292.15px immediately,
  435.22px at 100ms, and 460px after settling, proving eased movement.
- At 390x844, `/view100` and compact `/view103` both rendered one visible
  tooltip and produced full-page screenshots exactly 390px wide. Browser logs
  were empty.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-tooltip-style/legacy-echarts-tooltip.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-style/vue-tooltip-desktop.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-style/vue-tooltip-mobile.png`
- `artifacts/runs/20260714-legacy-chart-tooltip-style/vue-tooltip-compact-mobile.png`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; its pinned ECharts 3.1.7
  distribution was exercised directly in an isolated browser probe instead.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this CSS-only slice.
