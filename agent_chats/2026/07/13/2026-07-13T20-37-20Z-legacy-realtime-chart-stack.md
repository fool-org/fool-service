# Legacy Realtime Chart Stack

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore compact realtime `stack: 'a'` coordinate behavior.
- Recalculate the stack after legend filtering.
- Keep normal top-level unique-name series independent.

## Changed Files

- `frontend/src/legacyChartGeometry.ts`
- `frontend/src/legacyChartGeometry.test.ts`
- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T20-37-20Z-legacy-realtime-chart-stack.md`

## Legacy Evidence

- Realtime `LineChartController` assigns every line, bar, and scatter series
  the same stack id `a`; top-level `getchartoption` uses each metadata name.
- ECharts 3.1.7 links visible same-stack series in order and accumulates only
  previous values with the current sign.
- Line/scatter coordinates and axis extents use cumulative values. Bar tops use
  those values, but bar baselines track prior bars separately for positive and
  negative stacks.
- A stacked line area uses the immediately preceding same-sign cumulative
  series as its base and inherits smoothness when that series is also a line.

## Implementation

- Extended the existing geometry module with parallel rendered-value and base
  matrices plus flattened domain values. Unit tests cover mixed types, both
  signs, two bars, consecutive lines, and the non-stacked branch.
- Compact charts consume stacked coordinates for line/scatter points, bar
  rectangles, labels, areas, and scale calculation. Tooltip text remains the
  raw metadata value like ECharts.
- Compact bars share one stack slot; normal charts retain grouped bars and raw
  coordinates. Hidden legend series leave geometry before recomputation.
- `LegacyChartPanel.vue` remains 291 lines; geometry is isolated in an 83-line
  pure module instead of adding View or DTO branches.

## Validation

- Full frontend suite: 163 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:b6d2a57789240d544bd6ed10b2786bb0ac9b82fad8e9e1ea7027db318736e774`.
- Authenticated `/view103` retained 100 bars, 200 raw value labels, and six
  `0..100,000` ticks. Its Price line ended at y=`66.49981` with Amount stacked.
- Hiding Amount removed it before stack calculation and moved the same Price
  endpoint to y=`66.5` without changing raw labels or tick range.
- At 390x844 the stacked endpoint remained `66.49981`, the compact pane was
  328px wide, and document scroll width remained exactly 390px.
- Authenticated `/view100` retained its eight-category non-compact chart; unit
  geometry directly proves the non-compact branch keeps raw values and bases.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-realtime-chart-stack/realtime-chart-stack-desktop.png`
- `artifacts/runs/20260714-legacy-realtime-chart-stack/realtime-chart-stack-mobile.png`

## Risks And Follow-Ups

- Old top-level series use metadata names as stack ids; duplicate-name metadata
  remains unseeded and requires its own comparison before claiming that edge.
- Zero-value bar height still requires a separate ECharts parity check.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
