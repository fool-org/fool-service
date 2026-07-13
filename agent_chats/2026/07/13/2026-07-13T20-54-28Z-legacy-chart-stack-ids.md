# Legacy Chart Stack Ids

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically and retaining View-first rendering and code reuse.

## Scope

- Restore top-level metadata-name stack ids from `swchartLine.js`.
- Retain realtime `stack: 'a'` behavior in the same chart model.
- Align same-stack bar slots and duplicate-name legend presentation.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/legacyChartGeometry.ts`
- `frontend/src/legacyChartGeometry.test.ts`
- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T20-54-28Z-legacy-chart-stack-ids.md`

## Legacy Evidence

- Top-level `getchartoption` sets every line, bar, and scatter `stack` to its
  `PrpShowName`; realtime `LineChartController` uses the common id `a`.
- ECharts 3.1.7 links each list only to the previous series with the same
  nonempty stack id, and cumulative values traverse that stack chain by sign.
- Its bar layout uses the stack id as the column key, while unstacked bars use
  their series index. Legend rendering suppresses repeated names after the
  first item and selection remains name-based.

## Implementation

- Added an optional stack id to the generic chart series model. List-backed
  View projection copies the metadata name; rolling detail initialization
  replaces it with `a`. No business DTO fields enter the renderer.
- Generalized the existing geometry helper to calculate matching-stack values,
  bases, predecessor indexes, and bar groups. Empty stack ids remain isolated.
- The shared Vue panel consumes those groups for bar width/x coordinates and
  line-area smoothness, and renders only the first legend item per name.
- `LegacyChartPanel.vue` remains 293 lines and geometry remains 99 lines.

## Validation

- Full frontend suite: 164 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Frontend Docker build and forced recreation passed. Deployed image id:
  `sha256:90068f504d7be53aff541f64dc78be20c933103432e8e9fada3e0ef5052b39dc`.
- Unit geometry proves interleaved `Shared` / `Other` series produce independent
  stacks, same-stack bar groups, and cumulative `5` / `9` coordinates.
- Authenticated `/view103` retained 100 bars, 99 zero bars, two legend items,
  and the stacked Price endpoint y=`66.49981`.
- Authenticated `/view100` retained eight bars, six zero bars, two legend items,
  and its eight existing category x positions.
- At 390x844 `/view103` retained 100 bars and two legends; its pane stayed at
  x=`31..359` and document scroll width remained exactly 390px.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-chart-stack-ids/chart-stack-ids-desktop.png`
- `artifacts/runs/20260714-legacy-chart-stack-ids/chart-stack-ids-mobile.png`

## Risks And Follow-Ups

- Current seeded View metadata has unique series names, so duplicate-name
  runtime geometry is covered by deterministic unit tests rather than a live
  View mutation.
- Complete page parity remains subject to further View-by-View comparison.
- The existing authenticated browser session was reused, so no fresh CAPTCHA
  was generated or read for this frontend-only slice.
